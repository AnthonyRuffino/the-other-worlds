package io.blocktyper.theotherworlds.visible.spec;

import io.blocktyper.theotherworlds.visible.RelativeState;

import java.util.Optional;

public interface EntityUpdate {
    String getId();

    Optional<Float> getNewX();

    Optional<Float> getNewY();

    Optional<Float> getNewRotation();

    Optional<RelativeState> getNewRelativeState();
}
