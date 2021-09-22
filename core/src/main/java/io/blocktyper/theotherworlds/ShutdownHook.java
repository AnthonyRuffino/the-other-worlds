package io.blocktyper.theotherworlds;

import com.badlogic.gdx.LifecycleListener;

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

        if (game.gameServer != null) {
            System.out.println("Stopping game server");
            game.gameServer.stop();
        }

        if (game.reconnectionTimer != null) {
            game.reconnectionTimer.cancel();
        }
    }
}
