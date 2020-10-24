package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.BodyDef;
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PluginEntityCreator extends PluginLoader {
    @Override
    default Map<String, WorldEntity> getNewWorldEntities() {
        return getEntityCreators().entrySet().stream()
                .flatMap(entityCreator ->
                        entityCreator.getValue().getNewThings().stream()
                                .map(newThing -> thingToWorldEntity(newThing, entityCreator.getKey(), BodyDef.BodyType.DynamicBody))
                )
                .collect(Collectors.toMap(WorldEntity::getId, Function.identity(), (a, b) -> a));
    }
}
