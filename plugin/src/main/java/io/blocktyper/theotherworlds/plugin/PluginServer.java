package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.World;
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;

import java.util.Map;

public interface PluginServer {
    long getTick();
    long getTickDelayMillis();
    World getWorld();
    Map<String, WorldEntity> getDynamicEntities();
}
