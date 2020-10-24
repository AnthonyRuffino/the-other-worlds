package io.blocktyper.theotherworlds.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import io.blocktyper.theotherworlds.TheOtherWorldsGame;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import io.blocktyper.theotherworlds.config.RootConfig;

public class DesktopLauncher {
    public static void main(String[] arg) {
        new DesktopLauncher().run();
    }

    private void run() {

        RootConfig config = FileUtils.getJsonNodeWithLocalOverride(
                getClass().getClassLoader(),
                "default_client_config.json",
                "./client_config.json",
                RootConfig.class
        ).orElseThrow(() ->
            new RuntimeException("Unable to load client configs." )
        );

//        config.lwjgl.setDisplayModeCallback = (originalConfig) -> {
//            originalConfig.height = 100;
//            originalConfig.width = 100;
//            originalConfig.resizable = true;
//            originalConfig.fullscreen = false;
//            return originalConfig;
//        };

        if (config.lwjgl.maxNetThreads < 0) {
            config.lwjgl.maxNetThreads = Integer.MAX_VALUE;
        }


        //new LwjglApplication(new TheOtherWorldsGame(config.gameConfig), new LwjglApplicationConfiguration());
        new LwjglApplication(new TheOtherWorldsGame(config.clientConfig), config.lwjgl);
        //new LwjglApplication(new MyGdxGame2(), config.lwjgl);
    }


}
