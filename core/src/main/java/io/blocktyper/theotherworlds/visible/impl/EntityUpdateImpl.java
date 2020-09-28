package io.blocktyper.theotherworlds.visible.impl;

import io.blocktyper.theotherworlds.visible.spec.Entity;
import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;
import io.blocktyper.theotherworlds.visible.RelativeState;

import java.util.Optional;

public class EntityUpdateImpl implements EntityUpdate {


    private String id;
    private Optional<Float> newX;
    private Optional<Float> newY;
    private Optional<Float> newRotation;
    private Optional<RelativeState> newRelativeState;

    public EntityUpdateImpl() {
    }

    public EntityUpdateImpl(String id, Optional<Float> newX, Optional<Float> newY, Optional<Float> newRotation, Optional<RelativeState> newRelativeState) {
        this.id = id;
        this.newX = newX;
        this.newY = newY;
        this.newRotation = newRotation;
        this.newRelativeState = newRelativeState;
    }

    public EntityUpdateImpl(String id, Entity entity) {
        this(id, Optional.of(entity.getLocation().x), Optional.of(entity.getLocation().y), Optional.of(entity.getRotation()), entity.getRelativeState());
    }

    public String getId() {
        return id;
    }

    public Optional<Float> getNewX() {
        return newX;
    }

    public Optional<Float> getNewY() {
        return newY;
    }

    public Optional<Float> getNewRotation() {
        return newRotation;
    }

    public Optional<RelativeState> getNewRelativeState() {
        return newRelativeState;
    }
}
