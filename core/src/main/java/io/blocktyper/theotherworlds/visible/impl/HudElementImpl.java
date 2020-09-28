package io.blocktyper.theotherworlds.visible.impl;

import com.badlogic.gdx.math.Vector3;
import io.blocktyper.theotherworlds.visible.spec.HudElement;
import io.blocktyper.theotherworlds.visible.RelativeState;

import java.util.Optional;

public class HudElementImpl extends EntityImpl implements HudElement {

    boolean scrollable;
    boolean selectable;
    boolean cancelable;
    Optional<RelativeState> relativeState;

    public HudElementImpl() {
    }

    public HudElementImpl(
            String id,
            Vector3 location,
            Float rotation,
            boolean scrollable,
            boolean selectable,
            boolean cancelable,
            Optional<RelativeState> relativeState
    ) {
        super(id, location, rotation);
        this.scrollable = scrollable;
        this.selectable = selectable;
        this.cancelable = cancelable;
        this.relativeState = relativeState;
    }

    @Override
    public boolean isScrollable() {
        return scrollable;
    }

    @Override
    public boolean isSelectable() {
        return selectable;
    }

    @Override
    public boolean isCancelable() {
        return cancelable;
    }

    @Override
    public Optional<RelativeState> getRelativeState() {
        return relativeState;
    }
}
