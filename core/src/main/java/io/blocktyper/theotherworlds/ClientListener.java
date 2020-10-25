package io.blocktyper.theotherworlds;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import io.blocktyper.theotherworlds.server.auth.AuthUtils;
import io.blocktyper.theotherworlds.server.messaging.*;

import java.util.Optional;
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
            if (response.success) {
                game.username = response.username;
            }
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
            if(imageResponse.bytes == null) {
                game.missingSprites.add(imageResponse.name);
            } else {
                String fileName = game.getUsersServersDirectory() + imageResponse.name;
                System.out.println("Saving image: " + fileName);
                game.requestedSprites.remove(imageResponse.name);
                FileUtils.writeFile(fileName, imageResponse.bytes);
            }
        } else if (object instanceof ControlBindings) {
            ControlBindings controlBindings = (ControlBindings) object;
            String controlBindingsPath = game.getUsersServersDirectory() + controlBindings.pluginName + "/controlBindings.json";
            Optional<JsonNode> localOverride = FileUtils.getLocalOverride(controlBindingsPath);

            JsonNode mergedControlBindings = FileUtils.merge(controlBindings, localOverride);

            try {
                String json = FileUtils.getPrettyString(mergedControlBindings);
                FileUtils.writeFile(controlBindingsPath, json.getBytes());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            FileUtils.deserializeJson(ControlBindings.class, mergedControlBindings).ifPresent(b -> {
                game.clientInputAdapter.addControlBindingsConfig(b);
                game.clientInputAdapter.remapControls();
            });

        }
    }
}
