package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.World;

public interface PluginServer {
    long getTick();
    long getTickDelayMillis();
    World getWorld();
}
