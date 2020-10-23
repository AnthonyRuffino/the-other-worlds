package io.blocktyper.theotherworlds.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.controls.ButtonBinding;
import io.blocktyper.theotherworlds.plugin.controls.KeyBinding;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameConfig {
    public JsonNode gameModeSettings;
    public Map<String, Map<String, ButtonBinding>> gameModeButtonBindings;
    public Map<String, Map<String, KeyBinding>> gameModeKeyBindings;
    public boolean startServer = true;
    public String host = "localhost";


    public <T> Optional<T> getSetting(Class<T> type, String... path) {
        return getValueFromSettings(type, gameModeSettings, path);
    }

    public static <T> Optional<T> getValueFromSettings(Class<T> type, JsonNode node, String... path) {

        final List<String> pathAsList;
        if (path.length == 1) {
            pathAsList = Arrays.asList(path[0].split("\\."));
        } else {
            pathAsList = Arrays.asList(path);
        }

        int i = 1;
        int depth = pathAsList.size();
        JsonNode currentNode = node;
        StringBuilder pathToLog = new StringBuilder();
        for (String key : pathAsList) {
            pathToLog.append(key);
            currentNode = currentNode.get(key);
            if (i == depth) {
                return GameConfig.<T>getValueFromSettings(type, currentNode).or(() -> {
                    System.out.println("Unable to find setting '" + pathToLog + "'");
                    return Optional.empty();
                });
            } else {
                pathToLog.append(".");
            }
            i++;
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getValueFromSettings(Class<T> type, JsonNode node) {
        if (node != null) {
            try {
                return getValueFromNode(node)
                        .filter(v -> {
                            boolean typeMatch = type.equals(v.getClass());
                            if (!typeMatch) {
                                System.out.println("Type mismatch for setting " + type.getTypeName() + ". " + node.toPrettyString());
                            }
                            return typeMatch;
                        })
                        .map(v -> (T) v);
            } catch (Exception ex) {
                System.out.println("Unable to cast setting " + node.toPrettyString());
            }
        }
        return Optional.empty();
    }

    private static Optional<Object> getValueFromNode(JsonNode node) {
        if (node.isBoolean()) {
            return Optional.of(node.booleanValue());
        }
        if (node.isDouble()) {
            return Optional.of(node.doubleValue());
        }
        if (node.isTextual()) {
            return Optional.of(node.textValue());
        }
        if (node.isMissingNode()) {
            return Optional.empty();
        }
        return Optional.of(node);
    }

    public Optional<Map<String, KeyBinding>> gameModeKeyBindings(String gameMode) {
        return Optional.ofNullable(gameModeKeyBindings.get(gameMode));
    }

    public Optional<Map<String, ButtonBinding>> gameModeButtonBindings(String gameMode) {
        return Optional.ofNullable(gameModeButtonBindings.get(gameMode));
    }
}
