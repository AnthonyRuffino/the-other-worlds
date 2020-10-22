package io.blocktyper.theotherworlds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class BaseGame extends ApplicationAdapter {
    private Integer originalWidthForResizing = null;
    private Integer originalHeightForResizing = null;


    protected int heightUponCreate = 0;
    protected int widthUponCreate = 0;
    protected OrthographicCamera camera;

    @Override
    public void create() {
        heightUponCreate = HEIGHT();
        widthUponCreate = WIDTH();

        camera = new OrthographicCamera(widthUponCreate, heightUponCreate);
        camera.zoom = 10;
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
}
