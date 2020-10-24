package io.blocktyper.theotherworlds.plugin.controls;

public class KeyBinding {
    public String listenerAction;
    public String checkedAction;

    public KeyBinding setListenerAction(String listenerAction) {
        this.listenerAction = listenerAction;
        return this;
    }

    public KeyBinding setCheckedAction(String checkedAction) {
        this.checkedAction = checkedAction;
        return this;
    }
}
