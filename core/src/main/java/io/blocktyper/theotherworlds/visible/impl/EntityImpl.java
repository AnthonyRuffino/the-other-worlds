package io.blocktyper.theotherworlds.visible.impl;

import com.badlogic.gdx.math.Vector3;
import io.blocktyper.theotherworlds.visible.spec.Entity;

import java.util.Objects;

public class EntityImpl implements Entity {

    private String id;
    private Vector3 location;
    private Float rotation;

    public EntityImpl() {
    }

    public EntityImpl(String id, Vector3 location, Float rotation) {
        this.id = id;
        this.location = location;
        this.rotation = rotation;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Vector3 getLocation() {
        return location;
    }

    @Override
    public Float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Float rotation) {
        this.rotation = rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityImpl that = (EntityImpl) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
