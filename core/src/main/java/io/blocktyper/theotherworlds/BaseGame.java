package io.blocktyper.theotherworlds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BaseGame extends ApplicationAdapter {
    private Integer originalWidthForResizing = null;
    private Integer originalHeightForResizing = null;


    protected int heightUponCreate = 0;
    protected int widthUponCreate = 0;
    protected OrthographicCamera camera;

    protected SpriteBatch worldSpriteBatch;
    protected SpriteBatch hudSpriteBatch;
    protected ShapeRenderer hudShapeRenderer;

    @Override
    public void create() {
        heightUponCreate = HEIGHT();
        widthUponCreate = WIDTH();

        camera = new OrthographicCamera(widthUponCreate, heightUponCreate);
        camera.zoom = 30;

        worldSpriteBatch = new SpriteBatch();
        hudSpriteBatch = new SpriteBatch();
        hudShapeRenderer = new ShapeRenderer();
        hudShapeRenderer.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        if (originalWidthForResizing == null || originalHeightForResizing == null) {
            originalWidthForResizing = WIDTH();
            originalHeightForResizing = HEIGHT();
        }
        super.resize(width, height);
    }


    @Override
    public void render() {
        super.render();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public static int HEIGHT() {
        return Gdx.graphics.getHeight();
    }

    public static int WIDTH() {
        return Gdx.graphics.getWidth();
    }

    public void processCommand(String command) {
        String[] parts = command.split(" ");
        if(parts == null || parts.length < 1) {
            return;
        }
        if(parts[0].equals("setColor")) {
            Color worldSpriteBatchColor = worldSpriteBatch.getColor();
            Color hudSpriteBatchColor = hudSpriteBatch.getColor();
            Color hudShapeRendererColor = hudShapeRenderer.getColor();


            for(int i = 1; i < parts.length; i++) {
                if(!parts[i].contains("=")) {
                    continue;
                }
                String[] channelAndValue = parts[i].split("=");
                String channel = channelAndValue[0];
                String valueRaw = channelAndValue[1];
                float value = Float.parseFloat(valueRaw);

                if(channel.equals("r")) {
                    worldSpriteBatchColor.r = value;
                    hudSpriteBatchColor.r = value;
                    hudShapeRendererColor.r = value;
                } else if(channel.equals("g")) {
                    worldSpriteBatchColor.g = value;
                    hudSpriteBatchColor.g = value;
                    hudShapeRendererColor.g = value;
                } else if(channel.equals("b")) {
                    worldSpriteBatchColor.b = value;
                    hudSpriteBatchColor.b = value;
                    hudShapeRendererColor.b = value;
                }
            }

            worldSpriteBatch.setColor(worldSpriteBatchColor);
            hudSpriteBatch.setColor(hudSpriteBatchColor);
            hudShapeRenderer.setColor(hudShapeRendererColor);
        }
    }
}
