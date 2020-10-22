package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.entities.Thing;

import java.util.List;
import java.util.Optional;

public interface Plugin {

    void init(PluginServer pluginServer, JsonNode config);

    default List<ContactListener> getContactListeners() {
        return List.of();
    }

    default List<Thing> getStaticThings() {
        return List.of();
    }

    default Optional<EntityCreator> getEntityCreator() {
        return Optional.empty();
    }
}
