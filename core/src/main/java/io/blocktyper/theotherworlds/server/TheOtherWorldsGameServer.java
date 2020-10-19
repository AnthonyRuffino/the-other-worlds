package io.blocktyper.theotherworlds.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import io.blocktyper.theotherworlds.server.messaging.KryoUtils;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityRemovals;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityUpdates;
import io.blocktyper.theotherworlds.server.world.WorldEntity;
import io.blocktyper.theotherworlds.server.world.WorldEntityUpdate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TheOtherWorldsGameServer {

    public static String USER_DATA_DIRECTORY = "./.data/server/users/";

    private long tick = 0l;
    private Timer timer;

    World world;

    Map<Integer, Set<String>> keysPressedPerConnection = new ConcurrentHashMap<>();
    Map<Integer, String> playerNameMap = new HashMap<>();
    Map<Integer, Connection> connectionMap = new HashMap<>();
    Map<Integer, Object> playerMap = new HashMap<>();

    private static final int BATCH_SIZE = 30;
    Server server = new Server(1000000, 1000000);
    Kryo kryo;


    private Map<String, WorldEntity> staticEntities = new ConcurrentHashMap();
    private Map<String, WorldEntity> dynamicEntities = new ConcurrentHashMap();


    public static float GRAVITY = -1000f;
    public static float TARGET_DELTA = 1f / 60f;
    private float accumulator = 0;
    private float TARGET_ACCUMULATOR = TARGET_DELTA / 10f;
    long last_time = System.nanoTime();


    public static void main(String[] arg) {
        new TheOtherWorldsGameServer().start();
    }

    public void start() {

        try {

            world = new World(new Vector2(0, GRAVITY), true);


            staticEntities.put("floor_01",
                    new WorldEntity(
                            "floor_01",
                            BodyDef.BodyType.StaticBody.getValue(),
                            world,
                            -1000,
                            -2000,
                            10000,
                            500,
                            tick,
                            tick,
                            tick,
                            tick,
                            "sun.jpg"
                    ));

            server = new Server();
            kryo = server.getKryo();

            KryoUtils.registerClasses(kryo);

            server.start();
            server.bind(54555, 54777);

            server.addListener(new ServerListener(this));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected exception on server start: " + e.getMessage());
        }

        TimerTask task = new TimerTask() {
            public void run() {
                tick();
            }
        };
        timer = new Timer("Timer");

        long delay = 50L;
        timer.schedule(task, delay, delay);
    }


    private void doPhysicsStep(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= (TARGET_ACCUMULATOR)) {
            world.step(TARGET_ACCUMULATOR, 6, 2);
            accumulator -= TARGET_ACCUMULATOR;
        }
    }

    private long getDeltaTime() {
        long time = System.nanoTime();
        long delta_time = ((time - last_time) / 1000000);
        last_time = time;
        return delta_time;
    }

    public Map<String, WorldEntity> getStaticEntities() {
        return staticEntities;
    }

    private void tick() {

        tick++;
        long delta_time = getDeltaTime();

        if (tick % 2 == 0 && tick < 100000) {
            //System.out.println("entity");

            String entityId = "e_" + tick;

            dynamicEntities.put(
                    entityId,
                    new WorldEntity(
                            entityId,
                            BodyDef.BodyType.DynamicBody.getValue(),
                            world,
                            0,
                            1000,//(tick * (tick / 4)),
                            100,
                            100,
                            tick,
                            tick,
                            tick,
                            tick,
                            "sun.jpg"
                    ).setDeathTick(tick + 100)
            );
        }


        List<String> removals = new ArrayList<>();


        List<String> removedEntityIds = dynamicEntities.keySet().stream()
                .map(entityId -> dynamicEntities.get(entityId))
                .filter(entity -> entity.getDeathTick() != null && entity.getDeathTick() < tick)
                .map(entity -> {
                    world.destroyBody(entity.getBody());
                    dynamicEntities.remove(entity.getId());
                    return entity.getId();
                })
                .collect(Collectors.toList());

        removedEntityIds.forEach(dynamicEntities::remove);

        sendTCPToPlayers(
                () -> removedEntityIds,
                WorldEntityRemovals::new,
                BATCH_SIZE
        );

        Map<String, WorldEntityUpdate> beforePhysicsState = getDynamicEntitiesAsUpdates();

        processPlayerMovementActions(delta_time);
        doPhysicsStep(delta_time);

        sendUDPToPlayers(
                () -> gatherUpdatesFromPhysics(beforePhysicsState),
                WorldEntityUpdates::new,
                BATCH_SIZE
        );


        //applyNewPlayerPositions();
        //applyEntityPositions();

        //sendTCP updates
        //sendUDP updates
    }

    private synchronized <T> void sendUDPToPlayers(Supplier<List<T>> supplier,
                                                   Function<List<T>, Object> subListAcceptor,
                                                   int batchSize) {

        sendToPlayers(supplier, subListAcceptor, batchSize, Connection::sendUDP);
    }

    private synchronized <T> void sendTCPToPlayers(Supplier<List<T>> supplier,
                                                   Function<List<T>, Object> subListAcceptor,
                                                   int batchSize) {

        sendToPlayers(supplier, subListAcceptor, batchSize, Connection::sendTCP);
    }

    private synchronized <T> void sendToPlayers(Supplier<List<T>> supplier,
                                                Function<List<T>, Object> subListAcceptor,
                                                int batchSize,
                                                BiConsumer<Connection, Object> dispatchFunction) {
        try {
            synchronized (connectionMap) {
                List<T> data = supplier.get();
                if (data == null || data.isEmpty()) {
                    return;
                }
                int size = data.size();
                Set<Integer> connectionsToRemove = new HashSet<>();
                IntStream.range(0, (data.size() + batchSize - 1) / batchSize)
                        .mapToObj(i -> data.subList(i * batchSize, Math.min(size, (i + 1) * batchSize)))
                        .forEach(batch -> connectionMap.values()
                                .forEach(
                                        connection -> {
                                            try {
                                                dispatchFunction.accept(connection, subListAcceptor.apply(new ArrayList<>(batch)));
                                            } catch (Exception e) {
                                                System.out.println("Unexpected exception sending data to player: " + e.getMessage() + ". " + connection.getID());
                                                connectionsToRemove.add(connection.getID());
                                            }
                                        }
                                )
                        );
                connectionsToRemove.forEach(connectionMap::remove);
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception sending data to players: " + e.getMessage());
        }
    }


    @NotNull
    private List<WorldEntityUpdate> gatherUpdatesFromPhysics(Map<String, WorldEntityUpdate> beforePhysicsState) {
        synchronized (dynamicEntities) {
            return dynamicEntities.entrySet().stream().flatMap(
                    entry -> getUpdate(beforePhysicsState, entry).stream()
            ).collect(Collectors.toList());
        }
    }

    private Optional<WorldEntityUpdate> getUpdate(Map<String, WorldEntityUpdate> beforePhysicsState, Map.Entry<String, WorldEntity> entry) {
        return WorldEntityUpdate.getUpdate(
                beforePhysicsState.get(entry.getKey()),
                new WorldEntityUpdate(entry.getValue())
        );
    }

    @NotNull
    Map<String, WorldEntityUpdate> getDynamicEntitiesAsUpdates() {
        return getDynamicEntitiesAsUpdates(null);
    }

    Map<String, WorldEntityUpdate> getDynamicEntitiesAsUpdates(Set<String> filter) {
        Predicate<WorldEntity> predicate = filter == null ? (e) -> true : (e) -> filter.contains(e.getId());
        return dynamicEntities.values().stream()
                .filter(predicate)
                .map(WorldEntityUpdate::new)
                .collect(Collectors.toMap(WorldEntityUpdate::getId, value -> value));
    }

    Map<String, WorldEntityUpdate> getStaticEntitiesAsUpdates() {
        return staticEntities.values().stream()
                .map(WorldEntityUpdate::new)
                .collect(Collectors.toMap(WorldEntityUpdate::getId, value -> value));
    }


    void handleConnect(Connection connection, String playerName) {
        playerMap.put(connection.getID(), playerName);
        playerNameMap.put(connection.getID(), playerName);
        connectionMap.put(connection.getID(), connection);

        connection.sendTCP(new WorldEntityUpdates(
                new ArrayList<>(getStaticEntitiesAsUpdates().values())
        ).setMissing(true));
        //connection.sendTCP(new HudUpdates(getHudElementUpdates(playerName)));
    }


    private void processPlayerMovementActions(float deltaMod) {
        keysPressedPerConnection.forEach((key, value) ->
                movePlayer(deltaMod, value, null)
        );
    }

    private void movePlayer(float deltaMod, Set<String> keys, Body body) {
        boolean tryStrafe = false;
        boolean turnLeft = keys.contains("left");
        boolean turnRight = keys.contains("right");
        boolean forward = keys.contains("forward");
        boolean backward = keys.contains("back");
        boolean strafeLeft = false;
        boolean strafeRight = false;
    }
}
