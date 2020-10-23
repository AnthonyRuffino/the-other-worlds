package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.blocktyper.theotherworlds.config.ButtonBinding;
import io.blocktyper.theotherworlds.config.KeyBinding;
import io.blocktyper.theotherworlds.server.auth.AuthUtils;
import io.blocktyper.theotherworlds.server.messaging.PerformActionRequest;

import java.util.Map;
import java.util.Optional;

public class ClientInputAdapter extends InputAdapter {

    public static Map<Integer, String> BUTTON_CODE_MAP = Map.of(
            Input.Buttons.LEFT, "LEFT",
            Input.Buttons.MIDDLE, "MIDDLE",
            Input.Buttons.RIGHT, "RIGHT",
            Input.Buttons.FORWARD, "FORWARD",
            Input.Buttons.BACK, "BACK"
    );

    TheOtherWorldsGame game;
    AuthUtils authUtils;
    Input input;

    String lastCommand = "setColor r=1 g=1 b=1";

    Optional<Map<String, KeyBinding>> globalKeyBindings;
    Optional<Map<String, ButtonBinding>> globalButtonBindings;

    public ClientInputAdapter(TheOtherWorldsGame game, AuthUtils authUtils, Input input) {
        this.game = game;
        this.authUtils = authUtils;
        this.input = input;
        this.globalButtonBindings = Optional.ofNullable(game.config.gameModeButtonBindings.get("global"));
        this.globalKeyBindings = Optional.ofNullable(game.config.gameModeKeyBindings.get("global"));
    }

    @Override
    public boolean keyDown(int keycode) {
        sendKeyActions(Input.Keys.toString(keycode), game.config.gameModeKeyBindings(game.gameMode), false);
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {

        if (keycode == Input.Keys.ENTER) {

            input.getTextInput(
                    new Input.TextInputListener() {
                        @Override
                        public void input(String command) {
                            lastCommand = command;
                            game.processCommand(command);
                        }

                        @Override
                        public void canceled() {

                        }
                    },
                    "Command",
                    lastCommand,
                    null
            );

            return true;
        }


        sendKeyActions(Input.Keys.toString(keycode), game.config.gameModeKeyBindings(game.gameMode), true);
        return super.keyUp(keycode);
    }

    @Override
    public boolean scrolled(int amount) {
        int mod = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 2 : 1;
        game.camera.zoom += (.2f * amount * mod);
        if (game.camera.zoom < .2f) {
            game.camera.zoom = .2f;
        }
        return super.scrolled(amount);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        final String buttonCode = BUTTON_CODE_MAP.get(button);


        Optional<ButtonBinding> globalBinding = globalButtonBindings.map(b -> b.get(buttonCode));
        Optional<ButtonBinding> gameModeBinding = game.config.gameModeKeyButtonBindings(game.gameMode).map(b -> b.get(buttonCode));

        Optional<String> globalHudAction = globalBinding.flatMap(b -> Optional.ofNullable(b.hudAction));
        Optional<String> gameModeHudAction = gameModeBinding.flatMap(b -> Optional.ofNullable(b.hudAction));


//        if (globalHudAction.isPresent() || gameModeHudAction.isPresent()) {
//            for (HudVisual hudVisual : game.hudVisuals.values()) {
//                if (pointIsInRectangle(screenX, (game.HEIGHT() - screenY), hudVisual.x, hudVisual.width, hudVisual.y, hudVisual.height)) {
//
//                    globalHudAction.ifPresent(action -> {
//                        System.out.println("GLOBAL: " + hudVisual.entityUpdate.getId());
//                    });
//
//                    gameModeHudAction.ifPresent(action -> {
//                        System.out.println("GameMode: " + hudVisual.entityUpdate.getId());
//                        try {
//                            PerformActionRequest request = new PerformActionRequest();
//                            request.action = gameModeHudAction.get();
//                            request.target = hudVisual.getHudElementUpdate().getId();
//                            client.sendTCP(request);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            System.out.println("Unexpected exception sending command " + gameModeHudAction.get() + " " + hudVisual.getHudElementUpdate().getId() + ". Exception: " + e.getMessage());
//                        }
//                    });
//
//                    return true;
//                }
//            }
//        }

        return false;
    }


    void sendKeyActions(String key, Optional<Map<String, KeyBinding>> gameModeKeyBindings, boolean isCancel) {
        Optional<KeyBinding> globalBinding = globalKeyBindings.map(gmb -> gmb.get(key));
        Optional<KeyBinding> gameModeKeyBinding = gameModeKeyBindings.map(gmb -> gmb.get(key));

        globalBinding.ifPresent(keyBinding -> {
            PerformActionRequest request = new PerformActionRequest();
            request.action = keyBinding.listenerAction;
            request.cancel = isCancel;
            authUtils.getClient().sendTCP(request);
        });

        gameModeKeyBinding.ifPresent(keyBinding -> {
            PerformActionRequest request = new PerformActionRequest();
            request.action = keyBinding.listenerAction;
            request.cancel = isCancel;
            authUtils.getClient().sendTCP(request);
        });
    }

    boolean pointIsInRectangle(float pointX, float pointY, float rectangleX, float rectangleWidth, float rectangleY, float rectangleHeight) {
        return pointX > rectangleX && pointX < (rectangleX + rectangleWidth) && pointY > rectangleY && pointY < (rectangleY + rectangleHeight);
    }
}
