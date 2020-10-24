package io.blocktyper.theotherworlds.plugin.actions;

public class PlayerAction {
    public String player;
    public String actionName;
    public String target;
    public boolean cancel;

    public PlayerAction(String player, String actionName, String target, boolean cancel) {
        this.player = player;
        this.actionName = actionName;
        this.target = target;
        this.cancel = cancel;
    }
}
