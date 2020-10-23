package io.blocktyper.theotherworlds.server.auth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import io.blocktyper.theotherworlds.ClientListener;
import io.blocktyper.theotherworlds.TheOtherWorldsGame;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import io.blocktyper.theotherworlds.server.messaging.KryoUtils;
import io.blocktyper.theotherworlds.server.messaging.LoginRequest;

import java.security.KeyPair;
import java.util.function.Consumer;

import static io.blocktyper.theotherworlds.TheOtherWorldsGame.USER_DATA_DIRECTORY;

public class AuthUtils {

    TheOtherWorldsGame game;
    Client client;
    KeyPair keyPair;
    String username;
    String host;
    Consumer<Boolean> postReconnect;
    String lastChallenge;


    public AuthUtils(TheOtherWorldsGame game, Consumer<Boolean> postReconnect, String host) {
        this.game = game;
        this.postReconnect = postReconnect;
        this.host = host;
    }

    public LoginRequest loginRequest(byte[] publicKey, String captcha) {
        String signedChallenge = lastChallenge != null ? KeyUtils.sign(lastChallenge, keyPair.getPrivate()) : null;
        LoginRequest request = new LoginRequest();
        request.username = FileUtils.cleanFileName(username, false);
        request.signedChallenge = signedChallenge;
        request.publicKey = publicKey;
        request.captcha = captcha;
        return request;
    }

    public Client getClient() {
        if (client == null || !client.isConnected()) {
            setUpClient();
            if (username == null) {
                promptLogin(Gdx.input, USER_DATA_DIRECTORY);
            } else {
                login(null, null, null);
                postReconnect.accept(true);
            }
        }
        return client;
    }


    public void setUpClient() {
        this.client = new Client(1000000, 1000000);
        Kryo kryo = client.getKryo();
        KryoUtils.registerClasses(kryo);
        client.start();
        try {
            client.connect(5000, host, 54555, 54777);
        } catch (Exception ex) {
            System.out.println("Unable to connect: " + ex);
            ex.printStackTrace();
            return;
        }
        client.addListener(new ClientListener(game, this));
    }


    public void promptLogin(
            Input input,
            String userDataDirectory
    ) {
        promptLogin(input, "", "", false, userDataDirectory);
    }

    public void promptLogin(
            Input input,
            String previousError,
            String prefilledUserName,
            boolean newUser,
            String userDataDirectory
    ) {
        String title = (newUser ? "New User:" : "Login: ") + previousError;
        input.getTextInput(
                new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        if (!newUser) {
                            text = FileUtils.cleanFileName(text, false);
                            AuthUtils.this.username = text;
                        }

                        if (!text.isBlank()) {
                            keyPair = KeyUtils.loadKeyPair(userDataDirectory + AuthUtils.this.username + "/id_rsa", userDataDirectory + AuthUtils.this.username + "/id_rsa.pub");
                            login(newUser ? keyPair.getPublic().getEncoded() : null, null, newUser ? text : null);
                        } else {
                            promptLogin(input, "", "", newUser, userDataDirectory);
                        }
                    }

                    @Override
                    public void canceled() {
                        System.exit(-1);
                    }
                },
                title,
                prefilledUserName,
                prefilledUserName.isEmpty() ? (newUser ? "Captcha" : "username") : null
        );
    }

    public void login(byte[] publicKey, String challenge, String captcha) {
        if (challenge != null) {
            lastChallenge = challenge;
        }
        client.sendTCP(loginRequest(publicKey, captcha));
    }

}
