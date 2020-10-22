package io.blocktyper.theotherworlds.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.plugin.PluginLoader;
import io.blocktyper.theotherworlds.server.auth.KeyUtils;
import io.blocktyper.theotherworlds.server.messaging.*;

import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerListener extends Listener {


    private static final int CHALLENGE_EXPIRY_SECONDS = 10;
    private final TheOtherWorldsGameServer server;

    private Map<String, Map.Entry<String, Instant>> challenges = Collections.synchronizedMap(new HashMap<>());

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
        } else if (object instanceof ImageRequest) {
            handleImageRequest(connection, (ImageRequest) object);
        }
    }

    private void handleImageRequest(Connection connection, ImageRequest request) {

        String fullPath = request.name;
        String clientImageDirectory = PluginLoader.CLIENT_IMAGE_DIRECTORY;
        String imageWithPluginName = fullPath.substring(fullPath.indexOf(clientImageDirectory) + clientImageDirectory.length());
        int delimiterIndex = imageWithPluginName.indexOf("_");
        String pluginName = imageWithPluginName.substring(0, delimiterIndex);
        String imageName = imageWithPluginName.substring(delimiterIndex + 1);

        byte[] imageBytes = FileUtils.getLocalFileBytes("plugins/" + pluginName + "/img/" + imageName);
        connection.sendTCP(new ImageResponse().setName(fullPath).setBytes(imageBytes));
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
        String freshestChallenge;

        if (request.username == null || request.username.isBlank()) {
            response.success = false;
            response.newUser = false;
            response.message = "empty";
        } else {

            String publicKeyPath = TheOtherWorldsGameServer.USER_DATA_DIRECTORY + request.username + "/id_rsa.pub";

            byte[] publicKeyBytes = FileUtils.getLocalFileBytes(publicKeyPath);

            if (publicKeyBytes != null) {
                System.out.println("Existing user connection request: " + request.username);
                if ((freshestChallenge = getFreshestChallengeForUser(request)) == null) {
                    String challenge = UUID.randomUUID().toString();
                    challenges.put(request.username, new AbstractMap.SimpleEntry<>(challenge, Instant.now().plusSeconds(CHALLENGE_EXPIRY_SECONDS)));
                    response.username = request.username;
                    response.challenge = challenge;
                } else {
                    PublicKey publicKey;
                    try {
                        publicKey = KeyUtils.decodePublicKey(publicKeyBytes);
                    } catch (Exception ex) {
                        throw new RuntimeException("error recovering public key: " + ex.getMessage());
                    }

                    response.success = KeyUtils.verify(freshestChallenge, request.signedChallenge, publicKey);
                    response.message = response.success ? "User authenticated" : "user not authenticated";
                }
            } else if (request.publicKey != null) {
                System.out.println("New user creation attempt: " + request.username);
                FileUtils.writeFile(publicKeyPath, request.publicKey);
                response.success = true;
                response.message = "User created";
            } else {
                System.out.println("New user creation request: " + request.username);
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

    private String getFreshestChallengeForUser(LoginRequest loginRequest) {
        if (loginRequest.signedChallenge != null && !loginRequest.signedChallenge.isBlank()) {
            synchronized (challenges) {
                Map.Entry<String, Instant> lastChallenge = challenges.get(loginRequest.username);
                if (lastChallenge != null) {
                    if ((Instant.now()).isBefore(lastChallenge.getValue())) {
                        challenges.remove(loginRequest.username);
                        return lastChallenge.getKey();
                    } else {
                        System.out.println("Challenge expired: " + loginRequest.username);
                    }
                }
            }
        }
        return null;
    }

}
