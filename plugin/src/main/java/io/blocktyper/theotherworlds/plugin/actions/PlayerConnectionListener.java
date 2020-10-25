package io.blocktyper.theotherworlds.plugin.actions;

public interface PlayerConnectionListener {
    void handlePlayerConnection(String player, boolean isDisconnect);
}
