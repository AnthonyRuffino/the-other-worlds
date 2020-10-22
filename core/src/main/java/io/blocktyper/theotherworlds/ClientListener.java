package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.config.FileUtils;
import io.blocktyper.theotherworlds.server.auth.AuthUtils;
import io.blocktyper.theotherworlds.server.messaging.*;

import java.util.Set;

public class ClientListener extends Listener {

    TheOtherWorldsGame game;
    AuthUtils authUtils;

    public ClientListener(TheOtherWorldsGame game, AuthUtils authUtils) {
        this.game = game;
        this.authUtils = authUtils;
    }

    public void received(Connection connection, Object object) {

        if (object instanceof WorldEntityUpdates) {
            WorldEntityUpdates worldEntityUpdates = (WorldEntityUpdates) object;
            Set<String> missingEntities = game.addWorldEntityUpdates(worldEntityUpdates.getUpdates(), worldEntityUpdates.isMissing());


            if (!missingEntities.isEmpty()) {
                authUtils.getClient().sendTCP(new MissingWorldEntities(missingEntities));
            }
        } else if (object instanceof WorldEntityRemovals) {
            WorldEntityRemovals worldEntityRemovals = (WorldEntityRemovals) object;
            game.addWorldEntityRemovals(worldEntityRemovals.getRemovals());
        } else if (object instanceof ConnectResponse) {
            ConnectResponse response = (ConnectResponse) object;
            System.out.println("Connect status: " + response.success);
            game.clearHudShapes();
            if (response.captcha != null) {
                game.addHudShapeUpdates(response.captcha);
            }

            if (response.challenge != null) {
                game.authUtils.login(null, response.challenge, null);
            } else if (!response.success) {
                System.out.println(response.message);
                game.authUtils.promptLogin(
                        Gdx.input,
                        response.message,
                        response.username,
                        response.newUser,
                        TheOtherWorldsGame.USER_DATA_DIRECTORY
                );
            } else {
                //game.playerInstantiation = response.playerUpdate;
            }
        } else if (object instanceof ImageResponse) {
            ImageResponse imageResponse = (ImageResponse) object;
            FileUtils.writeFile(game.getServersDirectory() + imageResponse.name, imageResponse.bytes);
        }
    }
}
