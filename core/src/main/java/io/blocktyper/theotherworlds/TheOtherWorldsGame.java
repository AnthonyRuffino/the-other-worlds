package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.config.ButtonBinding;
import io.blocktyper.theotherworlds.config.GameConfig;
import io.blocktyper.theotherworlds.net.*;
import io.blocktyper.theotherworlds.net.auth.AuthUtils;
import io.blocktyper.theotherworlds.net.messaging.*;
import io.blocktyper.theotherworlds.visible.*;
import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;
import io.blocktyper.theotherworlds.visible.spec.HudElementUpdate;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//https://gamedev.stackexchange.com/questions/64092/running-multiple-box2d-world-objects-on-a-server
//https://www.gabrielgambetta.com/client-server-game-architecture.html
//https://github.com/EsotericSoftware/kryonet
public class TheOtherWorldsGame extends BaseGame {


    public static float GRAVITY = -1000;
    public static float NO_GRAVITY = 0;

    public static Map<Integer, String> BUTTON_CODE_MAP = Map.of(
            Input.Buttons.LEFT, "LEFT",
            Input.Buttons.MIDDLE, "MIDDLE",
            Input.Buttons.RIGHT, "RIGHT",
            Input.Buttons.FORWARD, "FORWARD",
            Input.Buttons.BACK, "BACK"
    );

    public static String USER_DATA_DIRECTORY = "./.data/users/";


    private SpriteBatch spriteBatch;
    private SpriteBatch hudBatch;
    private BitmapFont font;

    private final GameConfig config;
    private String gameMode = "play";

    private World world;

    private final Map<String, WorldEntity> worldEntities = new ConcurrentHashMap<>();
    private final Map<String, HudVisual> hudVisuals = new ConcurrentHashMap<>();

    private final Map<String, EntityUpdate> newWorldEntities = new ConcurrentHashMap<>();
    private final Map<String, HudElementUpdate> newHudVisuals = new ConcurrentHashMap<>();

    private TheOtherWorldsGameServer gameServer;
    private KeyPair keyPair;

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public TheOtherWorldsGame(GameConfig config) {
        this.config = config;

        if (this.config.startServer) {
            gameServer = new TheOtherWorldsGameServer();
            gameServer.start();
        }
    }


    private Client client;
    Kryo kryo;

    @Override
    public void create() {
        super.create();

        try {

            spriteBatch = new SpriteBatch();
            hudBatch = new SpriteBatch();
            world = new World(new Vector2(0, NO_GRAVITY), true);

            font = new BitmapFont();

            setUpClient();
            AuthUtils.promptLogin(Gdx.input, USER_DATA_DIRECTORY, this);


            Gdx.input.setInputProcessor(new InputAdapter() {
                @Override
                public boolean scrolled(int amount) {
                    int mod = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 2 : 1;
                    camera.zoom += (.2f * amount * mod);
                    if (camera.zoom < .2f) {
                        camera.zoom = .2f;
                    }
                    System.out.println("camera.zoom: " + camera.zoom);
                    return super.scrolled(amount);
                }

                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                    final String buttonCode = BUTTON_CODE_MAP.get(button);

                    Optional<ButtonBinding> globalBinding = Optional.ofNullable(config.gameModeButtonBindings.get("global")).map(b -> b.get(buttonCode));
                    Optional<ButtonBinding> gameModeBinding = Optional.ofNullable(config.gameModeButtonBindings.get(gameMode)).map(b -> b.get(buttonCode));

                    Optional<String> globalHudAction = globalBinding.flatMap(b -> Optional.ofNullable(b.hudAction));
                    Optional<String> gameModeHudAction = gameModeBinding.flatMap(b -> Optional.ofNullable(b.hudAction));


                    if (globalHudAction.isPresent() || gameModeHudAction.isPresent()) {
                        for (HudVisual hudVisual : hudVisuals.values()) {
                            if (pointIsInRectangle(screenX, (HEIGHT() - screenY), hudVisual.getX(), hudVisual.getWidth(), hudVisual.getY(), hudVisual.getHeight())) {

                                globalHudAction.ifPresent(action -> {
                                    System.out.println("GLOBAL: " + hudVisual.getEntityUpdate().getId());
                                });

                                gameModeHudAction.ifPresent(action -> {
                                    System.out.println("GameMode: " + hudVisual.getEntityUpdate().getId());
                                    try {
                                        PerformActionRequest request = new PerformActionRequest();
                                        request.action = gameModeHudAction.get();
                                        request.target = hudVisual.getHudElementUpdate().getId();
                                        client.sendTCP(request);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.out.println("Unexpected exception sending command " + gameModeHudAction.get() + " " + hudVisual.getHudElementUpdate().getId() + ". Exception: " + e.getMessage());
                                    }
                                });

                                return true;
                            }
                        }
                    }

                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected message creating game: " + e.getMessage());
        }
    }

    public void login(String username, byte[] publicKey) {
        client.sendTCP(AuthUtils.loginRequest(username, publicKey, keyPair.getPrivate()));
    }

    private void setUpClient() {
        client = new Client(1000000, 1000000);
        kryo = client.getKryo();

        KryoUtils.registerClasses(kryo);

        client.start();
        try{
            client.connect(5000, "localhost", 54555, 54777);
        } catch(Exception ex) {
            throw new RuntimeException("Unable to connect: " + ex);
        }

        TheOtherWorldsGame thiz = this;

        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof EntityUpdates) {
                    EntityUpdates response = (EntityUpdates) object;
                    saveNewEntityUpdates(response.entityUpdates);
                } else if (object instanceof EntityRemovals) {
                    EntityRemovals response = (EntityRemovals) object;
                    processEntityRemovals(response.entityRemovals);
                } else if (object instanceof HudUpdates) {
                    HudUpdates response = (HudUpdates) object;
                    saveNewHudUpdates(response.hudUpdates);
                } else if (object instanceof HudRemovals) {
                    HudRemovals response = (HudRemovals) object;
                    processHudRemovals(response.hudRemovals);
                } else if (object instanceof ConnectResponse) {
                    ConnectResponse response = (ConnectResponse) object;
                    System.out.println("Connect status: " + response.success);
                    if (!response.success) {
                        System.out.println(response.message);
                        AuthUtils.promptLogin(
                                Gdx.input,
                                response.message,
                                response.username,
                                response.newUser,
                                USER_DATA_DIRECTORY,
                                thiz
                        );
                    }
                }
            }
        });
    }

    private boolean pointIsInRectangle(float pointX, float pointY, float rectangleX, float rectangleWidth, float rectangleY, float rectangleHeight) {
        return pointX > rectangleX && pointX < (rectangleX + rectangleWidth) && pointY > rectangleY && pointY < (rectangleY + rectangleHeight);
    }


    void processHudRemovals(Collection<String> hudRemovals) {
        hudRemovals.forEach(hudVisuals::remove);
    }

    void saveNewHudUpdates(Collection<HudElementUpdate> hudElementUpdates) {
        hudElementUpdates.forEach(hudVisualUpdate -> {
            newHudVisuals.put(hudVisualUpdate.getId(), hudVisualUpdate);
        });
    }

    boolean processNewHudUpdates() {

        ArrayList<HudElementUpdate> hudElementUpdates;
        synchronized (newHudVisuals) {
            hudElementUpdates = new ArrayList<>(newHudVisuals.values());
            newHudVisuals.clear();
        }

        hudElementUpdates.forEach(hudVisualUpdate -> {

            if (!hudVisuals.containsKey(hudVisualUpdate.getId())) {
                hudVisuals.put(
                        hudVisualUpdate.getId(),
                        new HudVisual(SpriteUtils.newSprite(
                                hudVisualUpdate.getNewX().orElse(0f),
                                hudVisualUpdate.getNewY().orElse(0f),
                                hudVisualUpdate.getNewRotation().orElse(0f),
                                "sun.jpg"),
                                hudVisualUpdate)
                );
            }

            Visual visual = hudVisuals.get(hudVisualUpdate.getId());
            updateVisual(visual);
        });

        return !hudElementUpdates.isEmpty();
    }

    void processEntityRemovals(Collection<String> entityRemovals) {
        if (entityRemovals != null) {
            synchronized (worldEntities) {
                entityRemovals.forEach(entityRemoval -> {
                    System.out.println("entityRemoval " + worldEntities.remove(entityRemoval).getVisual().getEntityUpdate().getId());
                });
            }
        }
    }

    void saveNewEntityUpdates(Collection<EntityUpdate> entityUpdates) {
        entityUpdates.forEach(entityUpdate -> {
            newWorldEntities.put(entityUpdate.getId(), entityUpdate);
        });
    }

    void processNewEntityUpdates() {
        ArrayList<EntityUpdate> entityUpdates;
        synchronized (newWorldEntities) {
            entityUpdates = new ArrayList<>(newWorldEntities.values());
            newWorldEntities.clear();
        }

        entityUpdates.forEach(entityUpdate -> {

            if (!worldEntities.containsKey(entityUpdate.getId())) {
                System.out.println("add worldEntity " + entityUpdate.getId());
                worldEntities.put(entityUpdate.getId(), WorldEntity.box(
                        entityUpdate.getId(),
                        SpriteUtils.newSprite(
                                entityUpdate.getNewX().orElse(0f),
                                entityUpdate.getNewY().orElse(0f),
                                entityUpdate.getNewRotation().orElse(0f),
                                "morgan-blocksky.png"),
                        world,
                        .5f,
                        .5f,
                        .5f
                ));
            }

            Visual visual = worldEntities.get(entityUpdate.getId()).getVisual();
            visual.setEntityUpdate(entityUpdate);
            updateVisual(visual);
        });
    }


    @Override
    public void render() {
        super.render();


        //camToPos();

        camera.update();
        processNewEntityUpdates();
        if (processNewHudUpdates()) {
            resizeHud();
        }


        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        for (WorldEntity worldEntity : worldEntities.values()) {
            Sprite sprite = worldEntity.getVisual().getSprite(1);
            sprite.draw(spriteBatch);
        }
        spriteBatch.end();

        hudBatch.begin();
        for (Visual hudVisual : hudVisuals.values()) {
            //hudSprite.getSprite(1).draw(batch);
            Sprite sprite = hudVisual.getSprite(1);
            hudVisual.getRelativeState().ifPresentOrElse(relativeState -> {
                float width = getRelativeWidth(relativeState, sprite.getWidth());
                float height = getRelativeHeight(relativeState, sprite.getHeight());
                hudBatch.draw(sprite, sprite.getX(), sprite.getY(), hudVisual.getWidth(), hudVisual.getHeight());
            }, () -> {
                //sprite.draw(hudBatch);
            });
        }

        font.draw(hudBatch, "Text did not come from server", -WIDTH() / 2, HEIGHT() / 4);
        hudBatch.end();

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        resizeHud();
    }

    private void resizeHud() {
        hudVisuals.values().forEach(this::updateVisual);
    }

    private void updateVisual(Visual visual) {
        if (visual.getRelativeState().isPresent()) {

            RelativeState relativeState = visual.getRelativeState().get();

            Float relativeX = relativeState.getTransformX().orElse(0f);
            Float relativeY = relativeState.getTransformY().orElse(0f);

            Optional<Visual> relativeTo = Optional.ofNullable(hudVisuals.get(relativeState.getRelativeTo()));

            if (relativeTo.isPresent()) {
                Sprite sprite = relativeTo.get().getSprite(1);
                float width = getRelativeWidth(relativeState, sprite.getWidth());
                float height = getRelativeHeight(relativeState, sprite.getHeight());

                relativeX = relativeTo.get().getX() + (relativeX * width);
                relativeY = relativeTo.get().getY() + (relativeY * height);
            }

            visual.setX(relativeX);
            visual.setY(relativeY);

            visual.setWidth(getRelativeWidth(relativeState, visual.getSprite(1).getWidth()));
            visual.setHeight(getRelativeHeight(relativeState, visual.getSprite(1).getHeight()));

        } else {
            visual.getEntityUpdate().getNewX().ifPresent(visual::setX);
            visual.getEntityUpdate().getNewY().ifPresent(visual::setY);
        }


        visual.getEntityUpdate().getNewRotation().ifPresent(visual::setRotation);

    }

    private float getRelativeWidth(RelativeState relativeState, Float baseWidth) {
        return relativeState.getVw().map(vw -> (relativeState.isVwUseHeight() ? HEIGHT() : WIDTH()) * vw).orElse(baseWidth);
    }

    private float getRelativeHeight(RelativeState relativeState, Float baseHeight) {
        return relativeState.getVh().map(vh -> (relativeState.isVhUseWidth() ? WIDTH() : HEIGHT()) * vh).orElse(baseHeight);
    }

    @Override
    public void dispose() {

    }

}
