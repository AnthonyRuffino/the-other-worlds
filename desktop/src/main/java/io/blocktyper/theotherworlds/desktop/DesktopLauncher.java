package io.blocktyper.theotherworlds.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.blocktyper.theotherworlds.TheOtherWorldsGame;
import io.blocktyper.theotherworlds.config.ClientConfig;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

public class DesktopLauncher {
    public static void main(String[] arg) {
        new DesktopLauncher().run();
    }

    private void run() {

        ClientConfig config = FileUtils.getJsonNodeWithLocalOverride(
                getClass().getClassLoader(),
                "default_client_config.json",
                "./client_config.json",
                ClientConfig.class
        ).orElseThrow(() ->
                new RuntimeException("Unable to load client configs.")
        );

        Lwjgl3ApplicationConfiguration lwjglConfig = new Lwjgl3ApplicationConfiguration();
        lwjglConfig.setTitle("The Other Worlds");
        lwjglConfig.setWindowedMode(1536, 864);
        new Lwjgl3Application(new TheOtherWorldsGame(config), lwjglConfig);
    }


}
