package io.blocktyper.theotherworlds.visible;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteUtils {
    public static Sprite newSprite(String file) {
        return newSprite(0, 0, 0, file);
    }
    public static Sprite newSprite(float x, float y, float angle, String file) {
        Sprite sprite = new Sprite(new Texture(file));
        sprite.setOriginCenter();
        sprite.setPosition(x, y);
        sprite.rotate(angle);
        return sprite;
    }
}
