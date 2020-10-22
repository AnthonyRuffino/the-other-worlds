package io.blocktyper.theotherworlds.server.messaging;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface Drawable {
    String getId();

    void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch);
}
