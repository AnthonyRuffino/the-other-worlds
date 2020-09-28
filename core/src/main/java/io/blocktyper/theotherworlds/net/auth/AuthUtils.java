package io.blocktyper.theotherworlds.net.auth;

import com.badlogic.gdx.Input;
import io.blocktyper.theotherworlds.TheOtherWorldsGame;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.net.messaging.LoginRequest;

import java.security.KeyPair;
import java.security.PrivateKey;

public class AuthUtils {
    
    public static LoginRequest loginRequest(String username, byte[] publicKey, PrivateKey privateKey) {
        String signedMessage = KeyUtils.sign(username, privateKey);
        LoginRequest request = new LoginRequest();
        request.username = FileUtils.cleanFileName(username, false);
        request.signedUserName = signedMessage;
        request.publicKey = publicKey;
        return request;
    }


    public static void promptLogin(
            Input input,
            String userDataDirectory,
            TheOtherWorldsGame game
    ) {
        promptLogin(input, "", "", false, userDataDirectory, game);
    }
    public static void promptLogin(
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
                    KeyPair keyPair = KeyUtils.loadKeyPair(userDataDirectory + username + "/id_rsa", userDataDirectory + username + "/id_rsa.pub");
                    game.setKeyPair(keyPair);
                    game.login(username, newUser ? keyPair.getPublic().getEncoded() : null);
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
}
