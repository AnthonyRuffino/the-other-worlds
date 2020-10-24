package io.blocktyper.theotherworlds;


import io.blocktyper.theotherworlds.plugin.controls.ButtonBinding;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.controls.KeyBinding;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ClientInputAdapterTest {

    ClientInputAdapter clientInputAdapterTest;

    public ClientInputAdapterTest() {

    }

    @Test
    public void remapControls() {
        clientInputAdapterTest = new ClientInputAdapter(null, null, null);
        clientInputAdapterTest.addControlBindingsConfig(getControlBindings(1));
        clientInputAdapterTest.addControlBindingsConfig(getControlBindings(2));
        clientInputAdapterTest.remapControls();

        checkBindings(clientInputAdapterTest.globalButtonBindings, (a) -> a.hudAction, (a) -> a.worldAction, "global");
        checkBindings(clientInputAdapterTest.globalKeyBindings, (a) -> a.listenerAction, (a) -> a.checkedAction, "global");

        clientInputAdapterTest.gameModeButtonBindings.forEach((gameMode, bb) ->
            checkBindings(bb, (a) -> a.hudAction, (a) -> a.worldAction, gameMode)
        );

        clientInputAdapterTest.gameModeKeyBindings.forEach((gameMode, bb) ->
                checkBindings(bb, (a) -> a.listenerAction, (a) -> a.checkedAction, gameMode)
        );
    }

    private <T> void checkBindings(Map<String, List<T>> toCheck, Function<T, String> dataPointA, Function<T, String> dataPointB, String gameMode) {
        assert toCheck.size() == 4;
        assertAllButtonsHave2Actions(toCheck);
        checkBindings(toCheck, dataPointA, dataPointB, gameMode, 0);
        checkBindings(toCheck, dataPointA, dataPointB, gameMode, 1);
    }

    private <T> void checkBindings(Map<String, List<T>> toCheck, Function<T, String> dataPointA, Function<T, String> dataPointB, String gameMode, int index) {
        assertAllBindingsHaveCorrectActions(
                toCheck,
                index,
                dataPointA,
                dataPointB,
                getExpectedDataMap(index + 1, gameMode)
        );
    }

    @NotNull
    private Map<String, String> getExpectedDataMap(int pluginNumber, String gameMode) {
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("aa", gameMode + "Aa" + pluginNumber);
        expectedValues.put("ab", gameMode + "Ab" + pluginNumber);
        expectedValues.put("ba", gameMode + "Ba" + pluginNumber);
        expectedValues.put("bb", gameMode + "Bb" + pluginNumber);
        expectedValues.put("ca", null);
        expectedValues.put("cb", gameMode + "Cb" + pluginNumber);
        expectedValues.put("da", gameMode + "Da" + pluginNumber);
        expectedValues.put("db", null);
        return expectedValues;
    }

    private <T> void assertAllButtonsHave2Actions(Map<String, List<T>> map) {
        assert map.get("a").size() == 2;
        assert map.get("b").size() == 2;
        assert map.get("c").size() == 2;
        assert map.get("d").size() == 2;
    }

    private <T> void assertAllBindingsHaveCorrectActions(Map<String, List<T>> map, int index, Function<T, String> dataPointA, Function<T, String> dataPointB, Map<String, String> expectedValues) {
        assertEqual(dataPointA.apply(map.get("a").get(index)), expectedValues.get("aa"));
        assertEqual(dataPointA.apply(map.get("b").get(index)), expectedValues.get("ba"));
        assertEqual(dataPointA.apply(map.get("c").get(index)), expectedValues.get("ca"));
        assertEqual(dataPointA.apply(map.get("d").get(index)), expectedValues.get("da"));
        assertEqual(dataPointB.apply(map.get("a").get(index)), expectedValues.get("ab"));
        assertEqual(dataPointB.apply(map.get("b").get(index)), expectedValues.get("bb"));
        assertEqual(dataPointB.apply(map.get("c").get(index)), expectedValues.get("cb"));
        assertEqual(dataPointB.apply(map.get("d").get(index)), expectedValues.get("db"));
    }

    private void assertEqual(String expected, String actual) {
        if (expected == null) {
            assert actual == null;
        } else {
            assert expected.equals(actual);
        }
    }

    private ControlBindings getControlBindings(int pluginNumber) {
        ControlBindings controlBindings = new ControlBindings();
        controlBindings.pluginName = "plugin" + pluginNumber;
        controlBindings.gameModeButtonBindings = new HashMap<>();
        controlBindings.gameModeKeyBindings = new HashMap<>();

        controlBindings.gameModeButtonBindings.put("global", new HashMap<>());
        addButtonBindings(controlBindings.gameModeButtonBindings, pluginNumber, "global");

        controlBindings.gameModeButtonBindings.put("play", new HashMap<>());
        addButtonBindings(controlBindings.gameModeButtonBindings, pluginNumber, "play");

        controlBindings.gameModeKeyBindings.put("global", new HashMap<>());
        addKeyBindings(controlBindings.gameModeKeyBindings, pluginNumber, "global");

        controlBindings.gameModeKeyBindings.put("play", new HashMap<>());
        addKeyBindings(controlBindings.gameModeKeyBindings, pluginNumber, "play");

        return controlBindings;
    }

    private void addButtonBindings(Map<String, Map<String, ButtonBinding>> map, int pluginNumber, String gameMode) {
        map.get(gameMode)
                .put("a", bb("Aa", "Ab", gameMode, pluginNumber));

        map.get(gameMode)
                .put("b", bb("Ba", "Bb", gameMode, pluginNumber));

        map.get(gameMode)
                .put("c", bb(null, "Cb", gameMode, pluginNumber));

        map.get(gameMode)
                .put("d", bb("Da", null, gameMode, pluginNumber));
    }

    private void addKeyBindings(Map<String, Map<String, KeyBinding>> map, int pluginNumber, String gameMode) {
        map.get(gameMode)
                .put("a", kb("Aa", "Ab", gameMode, pluginNumber));

        map.get(gameMode)
                .put("b", kb("Ba", "Bb", gameMode, pluginNumber));

        map.get(gameMode)
                .put("c", kb(null, "Cb", gameMode, pluginNumber));

        map.get(gameMode)
                .put("d", kb("Da", null, gameMode, pluginNumber));
    }

    private ButtonBinding bb(String hudAction, String worldAction, String gameMode, int pluginNumber) {
        String hud = hudAction == null ? null : gameMode + hudAction + pluginNumber;
        String world = worldAction == null ? null : gameMode + worldAction + pluginNumber;
        return new ButtonBinding()
                .setHudAction(hud)
                .setWorldAction(world);
    }

    private KeyBinding kb(String listenerAction, String checkedAction, String gameMode, int pluginNumber) {
        String listener = listenerAction == null ? null : gameMode + listenerAction + pluginNumber;
        String checked = checkedAction == null ? null : gameMode + checkedAction + pluginNumber;
        return new KeyBinding()
                .setListenerAction(listener)
                .setCheckedAction(checked);
    }

}