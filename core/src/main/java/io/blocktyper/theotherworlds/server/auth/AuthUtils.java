package io.blocktyper.theotherworlds.server.auth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
    public boolean firstLoginComplete = false;


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
        if (firstLoginComplete && (client == null || !client.isConnected())) {
            setUpClient();
            if (username == null) {
                //throw new RuntimeException("Unable to determine user name when connecting!");
            } else {
                login(null, null, null);
                postReconnect.accept(true);
            }
        }
        return client;
    }


    public boolean setUpClient() {
        this.client = new Client(1000000, 1000000);
        Kryo kryo = client.getKryo();
        KryoUtils.registerClasses(kryo);
        client.start();
        try {
            client.connect(5000, host, 54555, 54777);
        } catch (Exception ex) {
            System.out.println("Unable to connect: " + ex.getMessage());
            ex.printStackTrace();
            if (loginMessageTextArea != null) {
                loginMessageTextArea.setText("Unable to connect: " + ex.getMessage());
            }
            return false;
        }
        client.addListener(new ClientListener(game, this));
        return true;
    }


    public void sendLoginInfo(
            boolean newUser,
            String userDataDirectory,
            String text
    ) {
        if (!newUser) {
            text = FileUtils.cleanFileName(text, false);
            AuthUtils.this.username = text;
        }

        if (!text.isBlank()) {
            keyPair = KeyUtils.loadKeyPair(userDataDirectory + AuthUtils.this.username + "/id_rsa", userDataDirectory + AuthUtils.this.username + "/id_rsa.pub");
            login(newUser ? keyPair.getPublic().getEncoded() : null, null, newUser ? text : null);
        } else {

        }
    }

    public void login(byte[] publicKey, String challenge, String captcha) {
        if (challenge != null) {
            lastChallenge = challenge;
        }
        client.sendTCP(loginRequest(publicKey, captcha));
    }


    //UI
    Skin skin;
    public TextField textField1;//host, captcha and commands (commands is short term hack)
    TextField usernameTextField;
    TextArea loginMessageTextArea;
    Label.LabelStyle textStyle;

    Label textField1Label;
    Label usernameLabel;
    Button loginButton;
    boolean newUser = false;
    BitmapFont font;

    public void captcha() {
        newUser = true;
        textField1Label.setText("Capthca");
        textField1.setText("");

        usernameLabel.remove();
        usernameTextField.remove();
        loginMessageTextArea.remove();
    }

    public Stage getLoginStage() {
        Stage stage = new Stage();
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        textField1 = new TextField(host, skin);
        usernameTextField = new TextField("admin", skin);
        loginMessageTextArea = new TextArea("", skin);


        font = new BitmapFont();
        textStyle = new Label.LabelStyle();
        textStyle.font = font;

        textField1Label = new Label("Host: ", textStyle);
        usernameLabel = new Label("Username: ", textStyle);

        loginButton = new TextButton("Login", skin, "default");
        loginButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (newUser) {
                    sendLoginInfo(true, USER_DATA_DIRECTORY, textField1.getText());
                } else {
                    host = textField1.getText();
                    if (setUpClient()) {
                        sendLoginInfo(false, USER_DATA_DIRECTORY, usernameTextField.getText());
                    }
                }
                return true;
            }
        });

        stage.addActor(loginMessageTextArea);
        stage.addActor(textField1);
        stage.addActor(usernameTextField);
        stage.addActor(textField1Label);
        stage.addActor(usernameLabel);
        stage.addActor(loginButton);

        sizeLoginUi();

        return stage;
    }


    private float stackActor(Actor actor, float x, float y, float width, float height) {
        actor.setX(x);
        actor.setY(y);
        actor.setWidth(width);
        actor.setHeight(height);
        return y + height;
    }

    private void sizeLoginUi() {
        float startingY = 10;
        startingY = stackActor(loginButton, 10, startingY, 400, 30);
        startingY = stackActor(loginMessageTextArea, 10, startingY, 400, 200);
        startingY = stackActor(usernameTextField, 10, startingY, 400, 30);
        startingY = stackActor(usernameLabel, 10, startingY, 400, 30);

        startingY = stackActor(textField1, 10, startingY, 400, 30);
        stackActor(textField1Label, 10, startingY, 400, 30);

        usernameLabel.setFontScale(1f, 1f);
        textField1Label.setFontScale(1f, 1f);
    }

    public String lastCommand = "/setColor r=1 g=1 b=1";
    public void finishLogin() {
        firstLoginComplete = true;
        loginMessageTextArea.remove();
        usernameTextField.remove();
        textField1Label.remove();
        usernameLabel.remove();
        loginButton.remove();

        textField1.setY(10);


        textField1.setText("/");

        game.stage.addCaptureListener(new InputListener() {

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                boolean superReturn = super.keyDown(event, keycode);
                if (keycode == Input.Keys.ENTER) {
                    lastCommand = textField1.getText();
                    game.processCommand(textField1.getText().replaceFirst("/", ""));
                    textField1.setText("");
                    game.useClientInputAdapter();
                    return true;
                }
                return superReturn;
            }
        });
    }
}
