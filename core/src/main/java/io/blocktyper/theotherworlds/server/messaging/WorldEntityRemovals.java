package io.blocktyper.theotherworlds.server.messaging;

import io.blocktyper.theotherworlds.server.world.WorldEntityUpdate;

import java.util.List;
import java.util.function.Consumer;

public class WorldEntityRemovals {

    private List<String> removals;
    private boolean missing;

    private WorldEntityRemovals() {

    }

    public WorldEntityRemovals(List<String> updates) {
        this.removals = updates;
    }

    public List<String> getRemovals() {
        return removals;
    }

    public WorldEntityRemovals setRemovals(List<String> updates) {
        this.removals = updates;
        return this;
    }

    public void send(Consumer<WorldEntityRemovals> send) {
        if (!removals.isEmpty()) {
            send.accept(this);
        }
    }
}
