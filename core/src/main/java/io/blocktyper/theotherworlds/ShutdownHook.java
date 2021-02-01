package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.controllers.Controllers;
import io.blocktyper.theotherworlds.server.TheOtherWorldsGameServer;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class ShutdownHook implements LifecycleListener {

    private TheOtherWorldsGame game;

    public ShutdownHook(TheOtherWorldsGame game) {
        this.game = game;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        System.out.println("Closing game");

        if(game.gameServer != null) {
            System.out.println("Stopping game server");
            game.gameServer.stop();
        }

        if(game.reconnectionTimer != null) {
            game.reconnectionTimer.cancel();
        }
    }
}
