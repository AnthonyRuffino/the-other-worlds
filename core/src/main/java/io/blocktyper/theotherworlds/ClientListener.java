package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import io.blocktyper.theotherworlds.server.messaging.ConnectResponse;
import io.blocktyper.theotherworlds.server.messaging.MissingWorldEntities;
import io.blocktyper.theotherworlds.server.messaging.WorldEntityUpdates;

import java.util.List;
import java.util.Set;

public class ClientListener extends Listener {

    TheOtherWorldsGame game;
    Client client;

    public ClientListener(TheOtherWorldsGame game, Client client) {
        this.game = game;
        this.client = client;
    }

    public void received(Connection connection, Object object) {
//        if (object instanceof EntityUpdates) {
//            EntityUpdates response = (EntityUpdates) object;
//            game.saveNewEntityUpdates(response.entityUpdates);
//        } else if (object instanceof EntityRemovals) {
//            EntityRemovals response = (EntityRemovals) object;
//            game.processEntityRemovals(response.entityRemovals);
//        } else if (object instanceof HudUpdates) {
//            HudUpdates response = (HudUpdates) object;
//            game.saveNewHudUpdates(response.hudUpdates);
//        } else if (object instanceof HudRemovals) {
//            HudRemovals response = (HudRemovals) object;
//            game.processHudRemovals(response.hudRemovals);
//        } else if (object instanceof EntityUpdate) {
//            EntityUpdate playerUpdate = (EntityUpdate) object;
//            game.savePlayerUpdate(playerUpdate);
//        } else if (object instanceof ConnectResponse) {
//            ConnectResponse response = (ConnectResponse) object;
//            System.out.println("Connect status: " + response.success);
//            if (!response.success) {
//                System.out.println(response.message);
//                game.authUtils.promptLogin(
//                        Gdx.input,
//                        response.message,
//                        response.username,
//                        response.newUser,
//                        game.USER_DATA_DIRECTORY,
//                        game
//                );
//            } else {
//                game.playerInstantiation = response.playerUpdate;
//            }
//        }
        if (object instanceof WorldEntityUpdates) {
            WorldEntityUpdates worldEntityUpdates = (WorldEntityUpdates) object;
            Set<String> missingEntities = game.addWorldEntityUpdates(worldEntityUpdates.getUpdates(), worldEntityUpdates.isMissing());
            if (!missingEntities.isEmpty()) {
                client.sendTCP(new MissingWorldEntities(missingEntities));
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
                        game.USER_DATA_DIRECTORY,
                        game
                );
            } else {
                //game.playerInstantiation = response.playerUpdate;
            }
        }
    }
}
