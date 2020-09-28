package io.blocktyper.theotherworlds.visible.spec;

import com.badlogic.gdx.math.Vector3;
import io.blocktyper.theotherworlds.visible.RelativeState;

import java.util.Optional;

public interface Entity {
    String getId();
    Vector3 getLocation();
    Float getRotation();
    void setRotation(Float rotation);
    default Optional<RelativeState> getRelativeState() {
        return Optional.empty();
    }

}
