package io.blocktyper.theotherworlds.visible.spec;

import io.blocktyper.theotherworlds.visible.spec.Entity;

public interface HudElement extends Entity {
    String getId();
    boolean isScrollable();
    boolean isSelectable();
    boolean isCancelable();
}
