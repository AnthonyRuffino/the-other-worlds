package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.BodyDef;
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;

import java.util.Map;
import java.util.stream.Collectors;

public interface PluginStaticWorldEntities extends PluginLoader {
    default Map<String, WorldEntity> getStaticWorldEntities() {
        return getPlugins().entrySet().stream().flatMap(plugin ->
                plugin.getValue().getStaticThings().stream()
                        .map(staticThing -> thingToWorldEntity(staticThing, plugin.getKey(), BodyDef.BodyType.StaticBody))
        ).collect(Collectors.toMap(e -> e.getId(), e -> e, (a, b) -> a));
    }
}
