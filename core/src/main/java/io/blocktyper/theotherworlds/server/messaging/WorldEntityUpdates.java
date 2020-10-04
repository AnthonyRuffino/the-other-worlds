package io.blocktyper.theotherworlds.server.messaging;

import io.blocktyper.theotherworlds.server.world.WorldEntityUpdate;

import java.util.List;
import java.util.function.Consumer;

public class WorldEntityUpdates {

    private List<WorldEntityUpdate> updates;
    private boolean missing;

    private WorldEntityUpdates() {

    }

    public WorldEntityUpdates(List<WorldEntityUpdate> updates) {
        this.updates = updates;
    }

    public List<WorldEntityUpdate> getUpdates() {
        return updates;
    }

    public WorldEntityUpdates setUpdates(List<WorldEntityUpdate> updates) {
        this.updates = updates;
        return this;
    }

    public boolean isMissing() {
        return missing;
    }

    public WorldEntityUpdates setMissing(boolean missing) {
        this.missing = missing;
        return this;
    }

    public void send(Consumer<WorldEntityUpdates> send) {
        if (!updates.isEmpty()) {
            send.accept(this);
        }
    }
}
