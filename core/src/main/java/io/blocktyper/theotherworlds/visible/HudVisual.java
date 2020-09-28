package io.blocktyper.theotherworlds.visible;

import com.badlogic.gdx.graphics.g2d.Sprite;
import io.blocktyper.theotherworlds.visible.spec.HudElementUpdate;

public class HudVisual extends Visual {

    private HudElementUpdate hudElementUpdate;


    public HudVisual(Sprite sprite, HudElementUpdate hudElementUpdate) {
        super(sprite, hudElementUpdate);
        this.hudElementUpdate = hudElementUpdate;
    }

    public HudElementUpdate getHudElementUpdate() {
        return hudElementUpdate;
    }
}
