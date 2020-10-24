package io.blocktyper.theotherworlds.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import io.blocktyper.theotherworlds.plugin.PluginLoader;
import io.blocktyper.theotherworlds.plugin.PluginLoaderImpl;
import io.blocktyper.theotherworlds.plugin.PluginServer;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;
import io.blocktyper.theotherworlds.server.messaging.KryoUtils;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityRemovals;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityUpdates;
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
import java.util.stream.Stream;

public class TheOtherWorldsGameServer implements PluginServer {

    public static String CWD = System.getProperty("user.dir");
    public static String USER_DATA_DIRECTORY = CWD + "/.data/server/users/";

    private long tick = 0l;
    private Timer timer;

    PluginLoader pluginLoader;
    Map<String, ControlBindings> pluginControlBindings;

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


    public static final long TICK_DELAY_MS = 30l;
    public static float GRAVITY = -1000f;
    public static float TARGET_DELTA = 1f / (1000 / TICK_DELAY_MS);
    private float accumulator = 0;
    private float TARGET_ACCUMULATOR = TARGET_DELTA / ((30f / 50f) * 50L);
    long last_time = System.nanoTime();


    public static void main(String[] arg) {
        new TheOtherWorldsGameServer().start();
    }

    public void start() {

        try {

            world = new World(new Vector2(0, GRAVITY), true);
            pluginLoader = new PluginLoaderImpl(CWD + "/plugins", this);
            pluginControlBindings = pluginLoader.getControlBindings();


            world.setContactListener(pluginLoader.getContactListener());
            staticEntities.putAll(pluginLoader.getStaticWorldEntities());

            server = new Server();
            kryo = server.getKryo();

            KryoUtils.registerClasses(kryo);

            server.start();
            server.bind(54555, 54777);

            server.addListener(new ServerListener(this));

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Unexpected exception on server start: " + ex.getMessage());
        }

        TimerTask task = new TimerTask() {
            public void run() {
                tick();
            }
        };
        timer = new Timer("TickTimer");

        timer.schedule(task, TICK_DELAY_MS, TICK_DELAY_MS);
    }

    @Override
    public long getTick() {
        return tick;
    }

    @Override
    public long getTickDelayMillis() {
        return TICK_DELAY_MS;
    }

    @Override
    public World getWorld() {
        return world;
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

    private void tick() {

        tick++;
        long delta_time = getDeltaTime();

        Map<String, WorldEntity> newWorldEntities = pluginLoader.getNewWorldEntities();
        dynamicEntities.putAll(newWorldEntities);

        doAllRemovals();

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

    private void doAllRemovals() {
        Stream<String> expiredEntityIds = dynamicEntities.keySet().stream()
                .map(entityId -> dynamicEntities.get(entityId))
                .filter(entity -> entity.getDeathTick() != null && entity.getDeathTick() < tick)
                .map(this::removeEntity);

        Stream<String> deadEntityIds = dynamicEntities.keySet().stream()
                .map(entityId -> dynamicEntities.get(entityId))
                .filter(WorldEntity::isDead)
                .map(this::removeEntity);

        sendTCPToPlayers(
                () -> Stream.concat(expiredEntityIds, deadEntityIds).collect(Collectors.toList()),
                WorldEntityRemovals::new,
                BATCH_SIZE
        );
    }

    private String removeEntity(WorldEntity entity) {
        world.destroyBody(entity.getBody());
        dynamicEntities.remove(entity.getId());
        return entity.getId();
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
                                            } catch (Exception ex) {
                                                System.out.println("Unexpected exception sending data to player: " + ex.getMessage() + ". " + connection.getID());
                                                connectionsToRemove.add(connection.getID());
                                            }
                                        }
                                )
                        );
                connectionsToRemove.forEach(connectionMap::remove);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected exception sending data to players: " + ex.getMessage());
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

    public Map<String, WorldEntity> getDynamicEntities() {
        return dynamicEntities;
    }

    void handleConnect(Connection connection, String playerName) {
        playerMap.put(connection.getID(), playerName);
        playerNameMap.put(connection.getID(), playerName);
        connectionMap.put(connection.getID(), connection);

        connection.sendTCP(new WorldEntityUpdates(
                new ArrayList<>(getStaticEntitiesAsUpdates().values())
        ).setMissing(true));

        pluginControlBindings.forEach((pluginName, controlBindings) ->
                connection.sendTCP(controlBindings.setPluginName(pluginName))
        );

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
