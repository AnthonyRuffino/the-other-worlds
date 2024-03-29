package io.blocktyper.theotherworlds.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import io.blocktyper.theotherworlds.server.auth.CaptchaUtils;
import io.blocktyper.theotherworlds.server.auth.KeyUtils;
import io.blocktyper.theotherworlds.server.messaging.*;

import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerListener extends Listener {


    private static final int CAPTCHA_EXPIRY_SECONDS = 60;
    private static final int CHALLENGE_EXPIRY_SECONDS = 10;
    private final TheOtherWorldsGameServer server;

    private final Map<String, Map.Entry<String, Instant>> challenges = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Map.Entry<String, Instant>> newUserCaptcha = Collections.synchronizedMap(new HashMap<>());

    public ServerListener(TheOtherWorldsGameServer server) {
        this.server = server;
    }

    public void received(Connection connection, Object requestObject) {
        if (requestObject instanceof LoginRequest) {
            handleLoginRequest(connection, (LoginRequest) requestObject);
        } else if (requestObject instanceof PerformActionRequest) {
            handlePerformActionRequest(connection, (PerformActionRequest) requestObject);
        } else if (requestObject instanceof MissingWorldEntities) {
            handleMissingWorldEntities(connection, (MissingWorldEntities) requestObject);
        } else if (requestObject instanceof ImageRequest) {
            handleImageRequest(connection, (ImageRequest) requestObject);
        }
    }

    private void handleImageRequest(Connection connection, ImageRequest request) {

        String imagePath = "plugins/" + request.name;
        byte[] imageBytes = FileUtils.getLocalFileBytes(imagePath);
        if (imageBytes == null) {
            System.out.println("Missing image on server: " + imagePath);
        }
        connection.sendTCP(new ImageResponse().setName(request.name).setBytes(imageBytes));
    }

    private void handleMissingWorldEntities(Connection connection, MissingWorldEntities request) {
        connection.sendTCP(
                new WorldEntityUpdates(
                        server.getDynamicEntitiesAsUpdates(request.getMissingEntities())
                                .values().parallelStream()
                                .collect(Collectors.toList())
                ).setMissing(true)
        );
    }

    private void handlePerformActionRequest(Connection connection, PerformActionRequest request) {

        Set<String> keysPressed = server.keysPressedPerConnection
                .computeIfAbsent(connection.getID(), k -> ConcurrentHashMap.newKeySet());

        server.pluginLoader.handlePlayerActions(server.playerNameMap.get(connection.getID()), request);

        if (request.cancel) {
            keysPressed.remove(request.action);
        } else {
            keysPressed.add(request.action);
        }
    }

    private void handleLoginRequest(Connection connection, LoginRequest request) {

        request.username = FileUtils.cleanFileName(request.username, false).toLowerCase();

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
                    response.signatureChallenge = challenge;
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

                String captcha = request.captcha;
                Map.Entry<String, Instant> existingCaptcha = newUserCaptcha.get(request.username);

                if (existingCaptcha == null
                        || captcha == null
                        || !captcha.equalsIgnoreCase(existingCaptcha.getKey())
                        || existingCaptcha.getValue().isBefore(Instant.now())
                ) {
                    System.out.println("Captcha failed: " + request.username);
                    generateCaptchaResponse(request, response);
                } else {
                    FileUtils.writeFile(publicKeyPath, request.publicKey);
                    response.success = true;
                    response.message = "User created";
                }

            } else {
                System.out.println("New user creation request: " + request.username);
                generateCaptchaResponse(request, response);
            }
        }

        if (response.success) {
            response.username = request.username;
        }

        connection.sendTCP(response);

        if (response.success) {
            server.handleConnect(connection, request.username);
        }

    }

    private void generateCaptchaResponse(LoginRequest request, ConnectResponse response) {
        Map.Entry<String, List<? extends Drawable>> captcha = CaptchaUtils.getCaptcha();
        newUserCaptcha.put(request.username, new AbstractMap.SimpleEntry<>(captcha.getKey(), Instant.now().plusSeconds(CAPTCHA_EXPIRY_SECONDS)));
        response.captcha = captcha.getValue();
        response.success = false;
        response.newUser = true;
        response.username = "";
        response.message = "Captcha [1-9]";
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
