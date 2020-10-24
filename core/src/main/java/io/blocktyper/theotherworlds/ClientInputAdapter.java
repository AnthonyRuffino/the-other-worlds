package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.blocktyper.theotherworlds.plugin.controls.ButtonBinding;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.controls.KeyBinding;
import io.blocktyper.theotherworlds.server.auth.AuthUtils;
import io.blocktyper.theotherworlds.server.messaging.PerformActionRequest;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    Map<String, ControlBindings> pluginControlBindings = new HashMap<>();

    Map<String, List<KeyBinding>> globalKeyBindings;
    Map<String, List<ButtonBinding>> globalButtonBindings;

    Map<String, Map<String, List<KeyBinding>>> gameModeKeyBindings;
    Map<String, Map<String, List<ButtonBinding>>> gameModeButtonBindings;

    public ClientInputAdapter(TheOtherWorldsGame game, AuthUtils authUtils, Input input) {
        this.game = game;
        this.authUtils = authUtils;
        this.input = input;

    }

    public void addControlBindingsConfig(ControlBindings controlBindings) {
        this.pluginControlBindings.put(controlBindings.pluginName, controlBindings);
    }

    synchronized public void remapControls() {
        Map<String, List<Map<String, ButtonBinding>>> buttonBindings = remapAsListOfMaps(cb -> cb.gameModeButtonBindings);
        Map<String, List<Map<String, KeyBinding>>> keyBindings = remapAsListOfMaps(cb -> cb.gameModeKeyBindings);

        globalButtonBindings = groupBindingsByGameMode(buttonBindings.get("global"));
        globalKeyBindings = groupBindingsByGameMode(keyBindings.get("global"));

        gameModeButtonBindings = groupBindingsByGameMode(buttonBindings);
        gameModeKeyBindings = groupBindingsByGameMode(keyBindings);
    }

    @NotNull
    private <T> Map<String, List<T>> groupBindingsByGameMode(List<Map<String, T>> bindingsForGameMode) {
        return bindingsForGameMode.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList()))
                );
    }

    private <T> Map<String, Map<String, List<T>>> groupBindingsByGameMode(Map<String, List<Map<String, T>>> bindings) {
        return bindings.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), groupBindingsByGameMode(e.getValue())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (a, b) -> {
                    System.out.println("Unexpected conflict mapping bindings: ");
                    return a;
                }));
    }


    private <T> Map<String, List<Map<String, T>>> remapAsListOfMaps(Function<ControlBindings, Map<String, Map<String, T>>> f) {
        return pluginControlBindings.values().stream()
                .flatMap(controlBindings -> f.apply(controlBindings).entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(
                                Map.Entry::getValue, Collectors.toList()
                        ))
                );
    }


    @Override
    public boolean keyDown(int keyCode) {
        String key = Input.Keys.toString(keyCode);
        sendKeyActions(key, false);
        return super.keyDown(keyCode);
    }

    @Override
    public boolean keyUp(int keyCode) {

        if (keyCode == Input.Keys.ENTER) {

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


        sendKeyActions(Input.Keys.toString(keyCode), true);
        return super.keyUp(keyCode);
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

        List<ButtonBinding> globalButtonBinding = getGlobalButtonBinding(buttonCode);
        List<ButtonBinding> gameModeButtonBinding = getGameModeButtonBinding(buttonCode);

        List<String> globalHudAction = globalButtonBinding.stream().map(b -> b.hudAction).collect(Collectors.toList());
        List<String> gameModeHudAction = gameModeButtonBinding.stream().map(b -> b.hudAction).collect(Collectors.toList());

//        if (globalHudAction.isEmpty() && gameModeHudAction.isEmpty()) {
//            return false;
//        }
//        for (HudVisual hudVisual : game.hudVisuals.values()) {
//            if (pointIsInRectangle(screenX, (game.HEIGHT() - screenY), hudVisual.x, hudVisual.width, hudVisual.y, hudVisual.height)) {
//
//                globalHudAction.ifPresent(action -> {
//                    System.out.println("GLOBAL: " + hudVisual.entityUpdate.getId());
//                });
//
//                gameModeHudAction.ifPresent(action -> {
//                    System.out.println("GameMode: " + hudVisual.entityUpdate.getId());
//                    try {
//                        PerformActionRequest request = new PerformActionRequest();
//                        request.action = gameModeHudAction.get();
//                        request.target = hudVisual.getHudElementUpdate().getId();
//                        client.sendTCP(request);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.out.println("Unexpected exception sending command " + gameModeHudAction.get() + " " + hudVisual.getHudElementUpdate().getId() + ". Exception: " + e.getMessage());
//                    }
//                });
//
//                return true;
//            }
//        }

        return false;
    }

    private List<ButtonBinding> getGlobalButtonBinding(String buttonCode) {
        return getSafeBindings(globalButtonBindings, buttonCode);
    }

    private List<KeyBinding> getGlobalKeyBinding(String keyCode) {
        return getSafeBindings(globalKeyBindings, keyCode);
    }

    private List<ButtonBinding> getGameModeButtonBinding(String buttonCode) {
        return Optional.ofNullable(gameModeButtonBindings)
                .map(gmbb -> getSafeBindings(gmbb.get(game.gameMode), buttonCode)).orElse(List.of());
    }

    private List<KeyBinding> getGameModeKeyBindings(String keyCode) {
        return Optional.ofNullable(gameModeKeyBindings)
                .map(gmbb -> getSafeBindings(gmbb.get(game.gameMode), keyCode)).orElse(List.of());
    }

    private <T> List<T> getSafeBindings(Map<String, List<T>> map, String keyCode) {
        return Optional.ofNullable(map).map(kb -> kb.get(keyCode)).orElse(List.of());
    }

    void sendKeyActions(String keyCode, boolean isCancel) {
        List<KeyBinding> globalKeyBinding = getGlobalKeyBinding(keyCode);
        List<KeyBinding> gameModeKeyBinding = getGameModeKeyBindings(keyCode);

        globalKeyBinding.forEach(keyBinding -> {
            PerformActionRequest request = new PerformActionRequest();
            request.action = keyBinding.listenerAction;
            request.cancel = isCancel;
            authUtils.getClient().sendTCP(request);
        });

        gameModeKeyBinding.forEach(keyBinding -> {
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
