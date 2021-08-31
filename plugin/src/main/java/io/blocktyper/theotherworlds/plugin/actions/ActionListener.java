package io.blocktyper.theotherworlds.plugin.actions;

import java.util.List;
import java.util.Set;

public interface ActionListener {
    Set<String> getInterests();

    void handlePlayerActions(List<PlayerAction> playerActions);
}
