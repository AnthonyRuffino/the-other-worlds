package io.blocktyper.theotherworlds.plugin.entities;

public interface Thing {
    String getId();

    String playerName();

    String getSpriteName();

    float getX();

    float getY();

    float getWidth();

    float getHeight();

    float getDensity();

    float getFriction();

    float getRestitution();

    float getAngle();

    default Long getDeathTick() {
        return null;
    }

    default Integer getHealth() {
        return null;
    }

    Float getLinearDampening();

}
