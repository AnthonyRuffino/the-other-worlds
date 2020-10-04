package io.blocktyper.theotherworlds.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.server.auth.KeyUtils;
import io.blocktyper.theotherworlds.server.messaging.*;

import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerListener extends Listener {


    private final TheOtherWorldsGameServer server;

    public ServerListener(TheOtherWorldsGameServer server) {
        this.server = server;
    }

    public void received(Connection connection, Object object) {
        if (object instanceof LoginRequest) {
            handleLoginRequest(connection, (LoginRequest) object);
        } else if (object instanceof PerformActionRequest) {
            handlePerformActionRequest(connection, (PerformActionRequest) object);
        } else if (object instanceof MissingWorldEntities) {
            handleMissingWorldEntities(connection, (MissingWorldEntities) object);
        }
    }

    private void handleMissingWorldEntities(Connection connection, MissingWorldEntities object) {
        connection.sendTCP(
                new WorldEntityUpdates(
                        server.getDynamicEntitiesAsUpdates(object.getMissingEntities())
                                .values().parallelStream()
                                .collect(Collectors.toList())
                ).setMissing(true)
        );
    }

    private void handlePerformActionRequest(Connection connection, PerformActionRequest request) {

        System.out.println("-------------------");
        System.out.println("action   - " + request.action);
        System.out.println("cancel   - " + request.cancel);
        System.out.println("target   - " + request.target);
        System.out.println("-------------------");

        if (
                "forward".equals(request.action)
                        || "back".equals(request.action)
                        || "left".equals(request.action)
                        || "right".equals(request.action)
        ) {
            Set<String> keysPressed = server.keysPressedPerConnection
                    .computeIfAbsent(connection.getID(), k -> ConcurrentHashMap.newKeySet());

            if (request.cancel) {
                keysPressed.remove(request.action);
            } else {
                keysPressed.add(request.action);
            }
        }
    }

    private void handleLoginRequest(Connection connection, LoginRequest request) {

        request.username = FileUtils.cleanFileName(request.username, false);

        ConnectResponse response = new ConnectResponse();

        if (request.username == null || request.username.isBlank()) {
            response.success = false;
            response.newUser = false;
            response.message = "empty";
        } else {
            System.out.println("user connected: " + request.username);

            String publicKeyPath = server.USER_DATA_DIRECTORY + request.username + "/id_rsa.pub";

            byte[] publicKeyBytes = FileUtils.getLocalFileBytes(publicKeyPath);

            if (publicKeyBytes != null) {
                PublicKey publicKey;
                try {
                    publicKey = KeyUtils.decodePublicKey(publicKeyBytes);
                } catch (Exception ex) {
                    throw new RuntimeException("error recovering public key: " + ex.getMessage());
                }

                response.success = KeyUtils.verify(request.username, request.signedUserName, publicKey);
                response.message = response.success ? "User authenticated" : "user not authenticated";

            } else if (request.publicKey != null) {
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


        if (response.success) {
            response.username = request.username;
            server.handleConnect(connection, request.username);
        }

        connection.sendTCP(response);
    }

}
