package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.server.auth.AuthUtils;
import io.blocktyper.theotherworlds.server.messaging.ConnectResponse;
import io.blocktyper.theotherworlds.server.messaging.MissingWorldEntities;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityUpdates;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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
        } else if (object instanceof ConnectResponse) {
            ConnectResponse response = (ConnectResponse) object;
            System.out.println("Connect status: " + response.success);
            if (!response.success) {
                System.out.println(response.message);
                game.authUtils.promptLogin(
                        Gdx.input,
                        response.message,
                        response.username,
                        response.newUser,
                        game.USER_DATA_DIRECTORY
                );
            } else {
                //game.playerInstantiation = response.playerUpdate;
            }
        }
    }
}
