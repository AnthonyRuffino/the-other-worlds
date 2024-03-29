package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.blocktyper.theotherworlds.config.ClientConfig;

import io.blocktyper.theotherworlds.server.TheOtherWorldsGameServer;
import io.blocktyper.theotherworlds.server.auth.AuthUtils;
import io.blocktyper.theotherworlds.server.messaging.Drawable;
import io.blocktyper.theotherworlds.server.messaging.ImageRequest;
import io.blocktyper.theotherworlds.server.world.WorldEntityUpdate;
import io.blocktyper.theotherworlds.visible.RelativeState;
import io.blocktyper.theotherworlds.visible.SpriteUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//https://gamedev.stackexchange.com/questions/64092/running-multiple-box2d-world-objects-on-a-server
//https://www.gabrielgambetta.com/client-server-game-architecture.html
//https://github.com/EsotericSoftware/kryonet
public class TheOtherWorldsGame extends BaseGame {

    public static String DATA_DIRECTORY = "./.data/";
    public static String USER_DATA_DIRECTORY = DATA_DIRECTORY + "users/";

    String host;

    final ClientConfig clientConfig;
    String gameMode = "play";


    Timer reconnectionTimer;


    final Set<String> missingSprites = new HashSet<>();
    final Set<String> requestedSprites = new HashSet<>();
    private final Map<String, Sprite> spriteMap = new ConcurrentHashMap<>();

    private final List<WorldEntityUpdate> worldEntityUpdates = new ArrayList<>();
    private final List<String> worldEntityRemovals = new ArrayList<>();
    private final Map<String, WorldEntityUpdate> worldEntities = new ConcurrentHashMap<>();

    private final List<Drawable> hudShapeUpdates = new ArrayList<>();
    private final Map<String, Drawable> hudShapes = new ConcurrentHashMap<>();
    private final List<String> hudShapeRemovals = new ArrayList<>();

    TheOtherWorldsGameServer gameServer;
    AuthUtils authUtils;
    String username;

    ClientInputAdapter clientInputAdapter;

    float camOffset = 0f;

    public Stage stage;


    public TheOtherWorldsGame(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;

        if (this.clientConfig.startServer) {
            gameServer = new TheOtherWorldsGameServer();
            gameServer.start();
        }
    }

    @Override
    public void create() {
        super.create();

        try {
            host = clientConfig.host == null ? "localhost" : clientConfig.host;
            authUtils = new AuthUtils(this, this::postReconnect, host);
            stage = authUtils.getLoginStage();

            useStageInputProcessor();

            Gdx.app.addLifecycleListener(new ShutdownHook(this));

            clientInputAdapter = new ClientInputAdapter(this, authUtils, Gdx.input);
            Controllers.addListener(clientInputAdapter);

            scheduleReconnector();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Unexpected message creating game: " + ex.getMessage());
        }
    }

    public void finishLogin() {
        authUtils.finishLogin();
        useClientInputAdapter();
    }

    public void useClientInputAdapter() {
        stage.unfocusAll();
        Gdx.input.setInputProcessor(clientInputAdapter);
    }

    public void useStageInputProcessor() {
        stage.setKeyboardFocus(authUtils.textField1);
        if (authUtils.firstLoginComplete) {
            authUtils.textField1.setText(authUtils.lastCommand);
        }
        authUtils.textField1.selectAll();
        Gdx.input.setInputProcessor(stage);
    }

    private void scheduleReconnector() {
        TimerTask task = new TimerTask() {
            public void run() {
                authUtils.getClient();
            }
        };
        reconnectionTimer = new Timer("ReconnectionTimer");

        reconnectionTimer.schedule(task, 2000L, 2000L);
    }


    Set<String> addWorldEntityUpdates(List<WorldEntityUpdate> worldEntityUpdates, boolean expectedMissing) {
        synchronized (this.worldEntityUpdates) {
            return worldEntityUpdates.stream().flatMap(update -> {
                if (worldEntities.containsKey(update.getId()) || expectedMissing) {
                    this.worldEntityUpdates.add(update);
                    return Stream.empty();
                }
                return Stream.of(update.getId());
            }).collect(Collectors.toSet());
        }
    }

    public void addWorldEntityRemovals(List<String> removals) {
        synchronized (this.worldEntityRemovals) {
            worldEntityRemovals.addAll(removals);
        }
    }


    void addHudShapeUpdates(List<? extends Drawable> floorDrawingUpdates) {
        synchronized (this.hudShapeUpdates) {
            this.hudShapeUpdates.addAll(floorDrawingUpdates);
        }
    }

    public void addHudShapeRemovals(List<String> removals) {
        synchronized (this.hudShapeRemovals) {
            hudShapeRemovals.addAll(removals);
        }
    }

    public void clearHudShapes() {
        synchronized (this.hudShapes) {
            hudShapes.clear();
        }
    }


    void postReconnect(boolean success) {
        //ask for updates and removals
    }


    @Override
    public void render() {
        super.render();

        if (stage != null) {
            stage.draw();
        }

        //add all new hudShapes
        synchronized (hudShapeUpdates) {
            hudShapeUpdates.forEach(update -> hudShapes.put(update.getId(), update));
            hudShapeUpdates.clear();
        }

        //do hudShape removals
        synchronized (hudShapeRemovals) {
            hudShapeRemovals.forEach(hudShapes::remove);
            hudShapeRemovals.clear();
        }


        //add player if does not exist

        //add all new world components
        synchronized (worldEntityUpdates) {
            worldEntityUpdates.forEach(update -> {
                WorldEntityUpdate entity = worldEntities.get(update.getId());
                if (entity == null) {
                    worldEntities.put(update.getId(), update);
                } else {
                    WorldEntityUpdate.applyUpdate(update, entity);
                }
            });
            worldEntityUpdates.clear();
        }


        //do world removals
        synchronized (worldEntityRemovals) {
            worldEntityRemovals.forEach(worldEntities::remove);
            worldEntityRemovals.clear();
        }


        //rotate sprite (for top-down 2d games)
        WorldEntityUpdate player;
        if (username != null && (player = worldEntities.get("player_" + username)) != null) {
            camera.position.x = player.getX().get();// - (player.getWidth() / 2);
            camera.position.y = player.getY().get();// - (player.getHeight() / 2);
        }
        //rotate camera (for top-down 2d games)
        camera.update();


        //add all new hud components
        //do hud removals

        hudSpriteBatch.begin();
        hudShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hudShapes.values().forEach(hudShape -> hudShape.draw(hudShapeRenderer, null));
        hudShapeRenderer.end();
        hudSpriteBatch.end();

        //draw all world components

        worldSpriteBatch.setProjectionMatrix(camera.combined);
        worldSpriteBatch.begin();

        /*
        Sprite sprite = new Sprite(textureStretched, Math.round(width), Math.round(height));
        sprite.setX(x-(width/2));
        sprite.setY(y-(height/2));
         */


        //draw all world entities
        worldEntities.forEach((key, entity) ->
                {
                    Sprite sprite = createSpriteIfNeeded(entity.getSpriteName().get());

                    entity.getxOrientation().ifPresent(xOrientation ->
                        sprite.flip("right".equals(xOrientation), false)
                    );

                    worldSpriteBatch.draw(sprite,
                            entity.getX().get() - (entity.getWidth().get() / 2),
                            entity.getY().get() - (entity.getHeight().get() / 2),
                            sprite.getX() - (sprite.getWidth() / 2),
                            sprite.getY() - (sprite.getHeight() / 2),
                            entity.getWidth().get(),
                            entity.getHeight().get()
                            , 1.0f, 1.0f, entity.getAngle().get()
                    );
                }
        );


        //spriteBatch.draw(sprite, worldEntity.x, worldEntity.y, worldEntity.width, worldEntity.height);

        //draw player
        //player.sprite.draw(spriteBatch);
        worldSpriteBatch.end();

        hudSpriteBatch.begin();
        //draw all hud items
        //hudBatch.draw(sprite, sprite.getX(), sprite.getY(), hudVisual.width, hudVisual.height);
        hudSpriteBatch.end();

    }


    private Sprite createSpriteIfNeeded(String spriteName) {
        if (spriteMap.containsKey(spriteName)) {
            return spriteMap.get(spriteName);
        }

        if (Gdx.files.internal(spriteName).exists()) {
            return tryLoadSprite(spriteName);
        } else if (Gdx.files.internal(getUsersServersDirectory() + spriteName).exists()) {
            return tryLoadSprite(getUsersServersDirectory() + spriteName);
        }

        if (!missingSprites.contains(spriteName)) {
            if (!requestedSprites.contains(spriteName)) {
                requestedSprites.add(spriteName);
                System.out.println("Requesting sprite: " + spriteName);
                authUtils.getClient().sendTCP(new ImageRequest().setName(spriteName));
            }
            return SpriteUtils.newSprite("loading.png");
        }

        return SpriteUtils.newSprite("missing.png");
    }

    @NotNull
    public String getUsersServersDirectory() {
        return USER_DATA_DIRECTORY + username + "/" + host + "/";
    }

    @NotNull
    private Sprite tryLoadSprite(String spriteName) {
        Sprite sprite;
        try {
            sprite = SpriteUtils.newSprite(spriteName);
        } catch (Exception e) {
            sprite = SpriteUtils.newSprite("missing.png");
            System.out.println("Issue loading sprite: " + spriteName + ". Message: " + e.getMessage());
        }
        spriteMap.put(spriteName, sprite);
        return sprite;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        resizeHud();
    }

    void resizeHud() {

    }

    float getRelativeWidth(RelativeState relativeState, Float baseWidth) {
        return relativeState.getVw().map(vw -> (relativeState.isVwUseHeight() ? HEIGHT() : WIDTH()) * vw).orElse(baseWidth);
    }

    float getRelativeHeight(RelativeState relativeState, Float baseHeight) {
        return relativeState.getVh().map(vh -> (relativeState.isVhUseWidth() ? WIDTH() : HEIGHT()) * vh).orElse(baseHeight);
    }

    @Override
    public void dispose() {

    }
}
