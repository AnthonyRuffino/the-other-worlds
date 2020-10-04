package io.blocktyper.theotherworlds.server.auth;

import com.badlogic.gdx.Input;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import io.blocktyper.theotherworlds.ClientListener;
import io.blocktyper.theotherworlds.TheOtherWorldsGame;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.server.messaging.KryoUtils;
import io.blocktyper.theotherworlds.server.messaging.LoginRequest;

import java.security.KeyPair;
import java.security.PrivateKey;

public class AuthUtils {

    TheOtherWorldsGame game;
    Client client;
    KeyPair keyPair;

    public AuthUtils(TheOtherWorldsGame game) {
        this.game = game;
    }

    public LoginRequest loginRequest(String username, byte[] publicKey, PrivateKey privateKey) {
        String signedMessage = KeyUtils.sign(username, privateKey);
        LoginRequest request = new LoginRequest();
        request.username = FileUtils.cleanFileName(username, false);
        request.signedUserName = signedMessage;
        request.publicKey = publicKey;
        return request;
    }


    public Client setUpClient() {
        this.client = new Client(1000000, 1000000);
        Kryo kryo = client.getKryo();
        KryoUtils.registerClasses(kryo);
        client.start();
        try{
            client.connect(5000, "localhost", 54555, 54777);
        } catch(Exception ex) {
            throw new RuntimeException("Unable to connect: " + ex);
        }
        client.addListener(new ClientListener(game, client));
        return client;
    }


    public void promptLogin(
            Input input,
            String userDataDirectory
    ) {
        promptLogin(input, "", "", false, userDataDirectory, game);
    }
    public void promptLogin(
            Input input,
            String previousError,
            String prefilledUserName,
            boolean newUser,
            String userDataDirectory,
            TheOtherWorldsGame game
    ) {
        input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String username) {
                username = FileUtils.cleanFileName(username, false);
                if(!username.isBlank()) {
                    keyPair = KeyUtils.loadKeyPair(userDataDirectory + username + "/id_rsa", userDataDirectory + username + "/id_rsa.pub");
                    login(username, newUser ? keyPair.getPublic().getEncoded() : null);
                } else {
                    promptLogin(input, "", "", false, userDataDirectory, game);
                }
            }

            @Override
            public void canceled() {
                System.exit(-1);
            }
        }, "Login: " + previousError, prefilledUserName, prefilledUserName.isEmpty() ? "username" : null);
    }

    public void login(String username, byte[] publicKey) {
        client.sendTCP(loginRequest(username, publicKey, keyPair.getPrivate()));
    }
}
