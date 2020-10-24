package io.blocktyper.theotherworlds.plugin.controls;

public class ButtonBinding {
    public String worldAction;
    public String hudAction;

    public ButtonBinding setWorldAction(String worldAction) {
        this.worldAction = worldAction;
        return this;
    }

    public ButtonBinding setHudAction(String hudAction) {
        this.hudAction = hudAction;
        return this;
    }
}
