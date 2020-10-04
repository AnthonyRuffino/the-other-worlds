package io.blocktyper.theotherworlds.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import io.blocktyper.theotherworlds.server.messaging.KryoUtils;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityUpdates;
import io.blocktyper.theotherworlds.server.world.WorldEntity;
import io.blocktyper.theotherworlds.server.world.WorldEntityUpdate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
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


    private Map<String, WorldEntity> worldEntities = new ConcurrentHashMap();


    public static float GRAVITY = 0f;
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

    private void tick() {

        tick++;
        long delta_time = getDeltaTime();

        if (tick % 20 == 0) {
            //System.out.println("entity");

            String entityId = "e_" + tick;

            worldEntities.put(
                    entityId,
                    new WorldEntity(
                            entityId,
                            world,
                            (tick * (tick / 4)),
                            (tick * (tick / 4)),
                            tick * (tick / 4),
                            tick * (tick / 4),
                            tick,
                            tick,
                            tick,
                            tick,
                            "mo.png"
                    )
            );
        }

        Map<String, WorldEntityUpdate> beforePhysicsState = getCurrentWorldEntityStates();

        processPlayerMovementActions(delta_time);
        doPhysicsStep(delta_time);

        new WorldEntityUpdates(gatherUpdatesFromPhysics(beforePhysicsState))
                .send(this::sendUpdatesToPlayers);


        //applyNewPlayerPositions();
        //applyEntityPositions();

        //sendTCP updates
        //sendUDP updates
    }

    private synchronized void sendUpdatesToPlayers(WorldEntityUpdates worldEntityUpdates) {
        try {
            synchronized (connectionMap) {
                List<WorldEntityUpdate> data = worldEntityUpdates.getUpdates();
                int size = data.size();
                Set<Integer> connectionsToRemove = new HashSet<>();
                IntStream.range(0, (worldEntityUpdates.getUpdates().size() + BATCH_SIZE - 1) / BATCH_SIZE)
                        .mapToObj(i -> data.subList(i * BATCH_SIZE, Math.min(size, (i + 1) * BATCH_SIZE)))
                        .forEach(batch -> connectionMap.values()
                                .forEach(
                                        connection -> {
                                            try {
                                                connection.sendUDP(new WorldEntityUpdates(new ArrayList<>(batch)));
                                            } catch (Exception e) {
                                                System.out.println("Unexpected exception sending updates to player: " + e.getMessage() + ". " + connection.getID());
                                                connectionsToRemove.add(connection.getID());
                                            }

                                        }
                                )
                        );
                connectionsToRemove.forEach(id -> connectionMap.remove(id));
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception sending updates to players: " + e.getMessage());
        }
    }

    @NotNull
    private List<WorldEntityUpdate> gatherUpdatesFromPhysics(Map<String, WorldEntityUpdate> beforePhysicsState) {
        synchronized (worldEntities) {
            return worldEntities.entrySet().stream().flatMap(
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
    Map<String, WorldEntityUpdate> getCurrentWorldEntityStates() {
        return getCurrentWorldEntityStates(null);
    }

    Map<String, WorldEntityUpdate> getCurrentWorldEntityStates(Set<String> filter) {
        Predicate<WorldEntity> predicate = filter == null ? (e) -> true : (e) -> filter.contains(e.getId());
        return worldEntities.values().stream()
                .filter(predicate)
                .map(WorldEntityUpdate::new)
                .collect(Collectors.toMap(WorldEntityUpdate::getId, value -> value));
    }


    void handleConnect(Connection connection, String playerName) {
        playerMap.put(connection.getID(), playerName);
        playerNameMap.put(connection.getID(), playerName);
        connectionMap.put(connection.getID(), connection);
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
