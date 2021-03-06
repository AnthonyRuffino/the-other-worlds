package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.actions.ActionListener;
import io.blocktyper.theotherworlds.plugin.actions.PlayerConnectionListener;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.entities.Thing;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

import java.util.List;
import java.util.Optional;

public interface Plugin {

    String CONFIG_NAME = "config.json";

    void init(String pluginName, PluginServer pluginServer, JsonNode config);

    JsonNode getConfig();

    default Optional<PlayerConnectionListener> getPlayerConnectionListener() {
        return Optional.empty();
    }

    default List<ContactListener> getContactListeners() {
        return List.of();
    }

    default List<Thing> getStaticThings() {
        return List.of();
    }

    default Optional<EntityCreator> getEntityCreator() {
        return Optional.empty();
    }

    default Optional<ActionListener> getActionListener() {
        return Optional.empty();
    }

    default Optional<ControlBindings> getControlBindings() {
        return Optional.ofNullable(getConfig()).flatMap(config ->
                FileUtils.deserializeJson(ControlBindings.class, config.get("controlBindings"))
        );
    }
}
