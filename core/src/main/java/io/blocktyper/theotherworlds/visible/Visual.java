package io.blocktyper.theotherworlds.visible;

import com.badlogic.gdx.graphics.g2d.Sprite;
import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;

import java.util.Optional;

public class Visual {
    private Sprite sprite;
    private float x;
    private float y;
    private float width;
    private float height;
    private float rotation;
    private EntityUpdate entityUpdate;


    public Visual(Sprite sprite, EntityUpdate entityUpdate) {
        this.sprite = sprite;
        this.x = sprite.getX();
        this.y = sprite.getY();
        this.rotation = sprite.getRotation();
        this.entityUpdate = entityUpdate;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setEntityUpdate(EntityUpdate entityUpdate) {
        this.entityUpdate = entityUpdate;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Optional<RelativeState> getRelativeState() {
        return Optional.ofNullable(entityUpdate).flatMap(EntityUpdate::getNewRelativeState);
    }

    public EntityUpdate getEntityUpdate() {
        return entityUpdate;
    }

    public Sprite getSprite(int worldTick) {
        sprite.setX(x);
        sprite.setY(y);
        sprite.setRotation(rotation);
        return sprite;
    }
}
