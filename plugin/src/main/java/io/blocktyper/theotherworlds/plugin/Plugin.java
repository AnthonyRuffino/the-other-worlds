package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.controls.ButtonBinding;
import io.blocktyper.theotherworlds.plugin.controls.KeyBinding;
import io.blocktyper.theotherworlds.plugin.entities.Thing;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Plugin {

    void init(PluginServer pluginServer, JsonNode config);
    JsonNode getConfig();

    default List<ContactListener> getContactListeners() {
        return List.of();
    }

    default List<Thing> getStaticThings() {
        return List.of();
    }

    default Optional<EntityCreator> getEntityCreator() {
        return Optional.empty();
    }

    default Map<String, Map<String, ButtonBinding>> getGameModeButtonBindings() {
        return Map.of("global", Map.of());
    }

    default Map<String, Map<String, KeyBinding>> gameModeKeyBindings() {
        return Map.of("global", Map.of());
    }

    default String getConfigResourcePath() {
        return "config.json";
    }
}
