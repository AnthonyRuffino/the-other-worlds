package io.blocktyper.theotherworlds.visible.impl;

import io.blocktyper.theotherworlds.visible.spec.HudElementUpdate;
import io.blocktyper.theotherworlds.visible.RelativeState;

import java.util.Optional;

public class HudElementUpdateImpl extends EntityUpdateImpl implements HudElementUpdate {

    private Optional<Boolean> newSelectable;
    private Optional<Boolean> newScrollable;
    private Optional<Boolean> newCancelable;

    public HudElementUpdateImpl() {
    }

    public HudElementUpdateImpl(
            String id,
            Optional<Float> newX,
            Optional<Float> newY,
            Optional<Float> rotation,
            Optional<Boolean> newSelectable,
            Optional<Boolean> newScrollable,
            Optional<Boolean> newCancelable,
            Optional<RelativeState> newRelativeState
    ) {
        super(id, newX, newY, rotation, newRelativeState);
        this.newSelectable = newSelectable;
        this.newScrollable = newScrollable;
        this.newCancelable = newCancelable;
    }



    @Override
    public Optional<Boolean> getNewIsSelectable() {
        return newSelectable;
    }

    @Override
    public Optional<Boolean> getNewCancelable() {
        return newCancelable;
    }

    @Override
    public Optional<Boolean> getNewIsScrollable() {
        return newScrollable;
    }
}
