package io.blocktyper.theotherworlds.visible;

import java.util.Optional;

public class RelativeState {
    private String relativeTo;
    private Optional<Float> transformX;
    private Optional<Float> transformY;
    private Optional<Float> vw;
    private Optional<Float> vh;
    private boolean vwUseHeight = false;
    private boolean vhUseWidth = true;

    public RelativeState() {
    }

    public RelativeState(String relativeTo, Optional<Float> transformX, Optional<Float> transformY, Optional<Float> vw, Optional<Float> vh) {
        this.relativeTo = relativeTo;
        this.transformX = transformX;
        this.transformY = transformY;
        this.vw = vw;
        this.vh = vh;
    }

    public String getRelativeTo() {
        return relativeTo;
    }

    public Optional<Float> getTransformX() {
        return transformX;
    }

    public Optional<Float> getTransformY() {
        return transformY;
    }

    public Optional<Float> getVw() {
        return vw;
    }

    public Optional<Float> getVh() {
        return vh;
    }

    public boolean isVwUseHeight() {
        return vwUseHeight;
    }

    public boolean isVhUseWidth() {
        return vhUseWidth;
    }

    public void setVwUseHeight(boolean vwUseHeight) {
        this.vwUseHeight = vwUseHeight;
    }

    public void setVhUseWidth(boolean vhUseWidth) {
        this.vhUseWidth = vhUseWidth;
    }
}
