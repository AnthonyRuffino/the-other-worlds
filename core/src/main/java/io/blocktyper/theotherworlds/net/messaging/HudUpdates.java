package io.blocktyper.theotherworlds.net.messaging;

import io.blocktyper.theotherworlds.visible.spec.HudElementUpdate;

import java.util.List;

public class HudUpdates {
    public List<HudElementUpdate> hudUpdates;

    public HudUpdates() {
    }

    public HudUpdates(List<HudElementUpdate> hudUpdates) {
        this.hudUpdates = hudUpdates;
    }

}
