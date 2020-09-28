package io.blocktyper.theotherworlds.visible.spec;

import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;

import java.util.Optional;

public interface HudElementUpdate extends EntityUpdate {
    String getId();
    Optional<Boolean> getNewIsSelectable();
    Optional<Boolean> getNewCancelable();
    Optional<Boolean> getNewIsScrollable();
}
