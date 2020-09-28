package io.blocktyper.theotherworlds.net.messaging;

import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;

import java.util.ArrayList;
import java.util.List;

public class EntityUpdates {
    public List<EntityUpdate> entityUpdates;

    public EntityUpdates() {
        entityUpdates = new ArrayList<>();
    }

    public EntityUpdates(List<EntityUpdate> entityUpdates) {
        this.entityUpdates = entityUpdates;
    }
}
