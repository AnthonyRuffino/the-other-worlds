package io.blocktyper.theotherworlds.server.messaging;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Line implements Drawable {
    public String id;
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public Float width;
    public Color color1;
    public Color color2;

    public Line() {
    }

    @Override
    public String getId() {
        return id;
    }

    public Line(String id, float x1, float y1, float x2, float y2, Float width, Color color1, Color color2) {
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.width = width;
        this.color1 = color1 == null ? Color.WHITE : color1;
        this.color2 = color2 == null ? this.color1 : color2;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {

        if (width != null) {
            shapeRenderer.rectLine(x1, y1, x2, y2, width, color1, color2);
        } else {
            shapeRenderer.setColor(color1);
            shapeRenderer.line(x1, y1, x2, y2);
        }
    }
}
