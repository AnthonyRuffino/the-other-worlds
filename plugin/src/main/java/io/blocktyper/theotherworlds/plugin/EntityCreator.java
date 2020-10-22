package io.blocktyper.theotherworlds.plugin;

import io.blocktyper.theotherworlds.plugin.entities.Thing;

import java.util.List;

public interface EntityCreator {
    List<Thing> getNewThings();
}
