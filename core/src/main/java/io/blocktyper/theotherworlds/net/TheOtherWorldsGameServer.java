package io.blocktyper.theotherworlds.net;

import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.net.auth.KeyUtils;
import io.blocktyper.theotherworlds.net.messaging.*;
import io.blocktyper.theotherworlds.visible.*;
import io.blocktyper.theotherworlds.visible.impl.EntityImpl;
import io.blocktyper.theotherworlds.visible.impl.EntityUpdateImpl;
import io.blocktyper.theotherworlds.visible.impl.HudElementImpl;
import io.blocktyper.theotherworlds.visible.impl.HudElementUpdateImpl;
import io.blocktyper.theotherworlds.visible.spec.Entity;
import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;
import io.blocktyper.theotherworlds.visible.spec.HudElement;
import io.blocktyper.theotherworlds.visible.spec.HudElementUpdate;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TheOtherWorldsGameServer {

    public static String USER_DATA_DIRECTORY = "./.data/server/users/";

    private long tick = 0l;
    private final Set<Entity> entities = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<? extends HudElement>> hudElements = new ConcurrentHashMap<>();
    private Timer timer;

    Map<String, Connection> connections = new HashMap<>();


    Server server = new Server(1000000, 1000000);
    Kryo kryo;

    public static void main(String[] arg) {
        new TheOtherWorldsGameServer().start();
    }

    public void start() {

        try {

            server = new Server();
            kryo = server.getKryo();

            KryoUtils.registerClasses(kryo);

            server.start();
            server.bind(54555, 54777);

            server.addListener(new Listener() {
                public void received(Connection connection, Object object) {
                    if (object instanceof LoginRequest) {
                        LoginRequest request = (LoginRequest) object;

                        request.username = FileUtils.cleanFileName(request.username, false);

                        ConnectResponse response = new ConnectResponse();

                        if(request.username == null || request.username.isBlank()) {
                            response.success = false;
                            response.newUser = false;
                            response.message = "empty";
                        } else {
                            System.out.println(request.username);

                            String publicKeyPath = USER_DATA_DIRECTORY + request.username + "/id_rsa.pub";

                            byte[] publicKeyBytes = FileUtils.getLocalFileBytes(publicKeyPath);

                            if(publicKeyBytes != null) {
                                PublicKey publicKey;
                                try {
                                    publicKey = KeyUtils.decodePublicKey(publicKeyBytes);
                                } catch (Exception ex) {
                                    throw new RuntimeException("error recovering public key: " + ex.getMessage());
                                }

                                response.success = KeyUtils.verify(request.username, request.signedUserName, publicKey);
                                response.message = response.success ? "User authenticated" : "user not authenticated";

                            } else if(request.publicKey != null) {
                                FileUtils.writeFile(publicKeyPath, request.publicKey);
                                response.success = true;
                                response.message = "User created";
                            } else {
                                response.success = false;
                                response.newUser = true;
                                response.username = request.username;
                                response.message = "Create new user?";
                            }
                        }

                        connection.sendTCP(response);

                        if(response.success) {
                            connections.put(request.username, connection);
                            handleConnect(connection, request.username);
                        }
                    } else if (object instanceof PerformActionRequest) {
                        PerformActionRequest request = (PerformActionRequest) object;

                        System.out.println("-------------------");
                        System.out.println("action   - " + request.action);
                        System.out.println("target   - " + request.target);
                        System.out.println("entities - " + entities.size());
                        System.out.println("-------------------");

                        addEntity(UUID.randomUUID().toString(), getRandomFloat(300), getRandomFloat(300), 0, getRandomFloat(1));

                    }
                }
            });

        } catch (Exception e) {
            System.out.println();
        }


        IntStream.range(1, 2).forEach(i -> {
            addEntity(i + "", getRandomFloat(300), getRandomFloat(300), 0, getRandomFloat(1));
        });

        TimerTask task = new TimerTask() {
            public void run() {
                tick();
            }
        };
        timer = new Timer("Timer");

        long delay = 50L;
        timer.schedule(task, delay, delay);
    }

    private void addEntity(String d, float x, float y, float z, float rotation) {
        entities.add(new EntityImpl(d, new Vector3(x, y, z), rotation));
    }

    private void tick() {
        tick++;
        List<String> removedEntityIds = null;
        List<EntityUpdate> entityUpdates = new ArrayList<>();
        for (Entity entity : entities) {
            entity.getLocation().x += getRandomDouble(10);
            entity.getLocation().y += getRandomDouble(10);
            entity.setRotation(entity.getRotation() + getRandomFloat(10));
            if (entity.getId().equals("1") && tick > 200) {
                entities.remove(entity);
                removedEntityIds = new ArrayList<>();
                removedEntityIds.add(entity.getId());
            } else {
                entityUpdates.add(getEntityUpdate(entity));
            }
        }

        sendClientUpdates(entityUpdates, removedEntityIds);
    }

    private float getRandomFloat(int scale) {
        return (float) getRandomDouble(scale);
    }

    private double getRandomDouble(int scale) {
        return ((Math.random() * scale) * (Math.random() >= .5 ? -1 : 1));
    }

    private void sendClientUpdates(List<EntityUpdate> updatedEntities, List<String> removedEntityIds) {
        for (Connection connection : connections.values()) {
            connection.sendTCP(new EntityUpdates(updatedEntities));
            connection.sendTCP(new EntityRemovals(removedEntityIds));
        }
    }

    private void handleConnect(Connection connection, String id) {
        List<EntityUpdate> entityUpdates = entities.stream().map(this::getEntityUpdate).collect(Collectors.toList());
        connection.sendTCP(new EntityUpdates(entityUpdates));
        connection.sendTCP(new HudUpdates(getHudElementUpdates(id)));
    }

    private List<HudElementUpdate> getHudElementUpdates(String id) {
        return hudElements.computeIfAbsent(id, (a) -> {
            Set<HudElement> hudElements = ConcurrentHashMap.newKeySet();


            hudElements.add(new HudElementImpl(
                    "action_bar_1_1",
                    new Vector3(100, 0, 0),
                    90f,
                    false,
                    true,
                    false,
                    Optional.of(new RelativeState(
                            "screen",
                            Optional.empty(),
                            Optional.empty(),
                            Optional.of(((float) (1.0 / 30.0))),
                            Optional.of(((float) (1.0 / 30.0)))
                    )))
            );
            hudElements.add(new HudElementImpl(
                    "action_bar_1_2",
                    new Vector3(0, 0, 0),
                    180f,
                    false,
                    false,
                    true,
                    Optional.of(new RelativeState(
                            "action_bar_1_1",
                            Optional.of(1f),
                            Optional.empty(),
                            Optional.of(((float) (1.0 / 30.0))),
                            Optional.of(((float) (1.0 / 30.0)))
                    )))
            );
//            hudElements.add(new HudElementImpl(
//                    "curse_1",
//                    new Vector3(0,0,0),
//                    0f,
//                    false,
//                    false,
//                    false)
//            );
            return hudElements;
        }).stream().map(hudElement ->
                new HudElementUpdateImpl(hudElement.getId(),
                        Optional.of(hudElement.getLocation().x),
                        Optional.of(hudElement.getLocation().y),
                        Optional.of(hudElement.getRotation()),
                        Optional.of(hudElement.isSelectable()),
                        Optional.of(hudElement.isScrollable()),
                        Optional.of(hudElement.isCancelable()),
                        hudElement.getRelativeState()
                )
        ).collect(Collectors.toList());
    }

    @NotNull
    private EntityUpdate getEntityUpdate(Entity entity) {
        return new EntityUpdateImpl(entity.getId(), entity);
    }


}
