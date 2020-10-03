package io.blocktyper.theotherworlds.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.blocktyper.theotherworlds.TheOtherWorldsGame;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.config.RootConfig;

import java.util.Optional;

public class DesktopLauncher {
    public static void main(String[] arg) {
        new DesktopLauncher().run();
    }

    private void run() {

        RootConfig config = FileUtils.getLocalLwjglApplicationConfig(
                getClass().getClassLoader(),
                "default_config.json",
                "./local_config.json"
        );

//        config.lwjgl.setDisplayModeCallback = (originalConfig) -> {
//            originalConfig.height = 100;
//            originalConfig.width = 100;
//            originalConfig.resizable = true;
//            originalConfig.fullscreen = false;
//            return originalConfig;
//        };

        Optional<Double> s = config.gameConfig.getSetting(Double.class, "play.BASE_SPEED");
        Optional<ArrayNode> d2 = config.gameConfig.getSetting(ArrayNode.class, "play.STARTING_SPELLS");

        if (config.lwjgl.maxNetThreads < 0) {
            config.lwjgl.maxNetThreads = Integer.MAX_VALUE;
        }





        //new LwjglApplication(new TheOtherWorldsGame(config.gameConfig), new LwjglApplicationConfiguration());
        new LwjglApplication(new TheOtherWorldsGame(config.gameConfig), config.lwjgl);
        //new LwjglApplication(new MyGdxGame2(), config.lwjgl);
    }


}
