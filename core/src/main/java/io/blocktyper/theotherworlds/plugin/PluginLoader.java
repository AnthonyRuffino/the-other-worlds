package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.entities.Thing;
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;
import io.blocktyper.theotherworlds.server.messaging.PerformActionRequest;

import java.util.Map;

public interface PluginLoader {
    String CLIENT_IMAGE_DIRECTORY = "img/";

    Map<String, Plugin> getPlugins();

    World getWorld();

    ContactListener getContactListener();

    Map<String, WorldEntity> getNewWorldEntities();

    Map<String, WorldEntity> getStaticWorldEntities();

    Map<String, EntityCreator> getEntityCreators();

    Map<String, ControlBindings> getControlBindings();

    void handlePlayerConnection(String player, boolean isDisconnect);

    void handleActions(String player, PerformActionRequest performActionRequest);

    default WorldEntity thingToWorldEntity(Thing thing, String pluginName, BodyDef.BodyType bodyType) {
        WorldEntity worldEntity = new WorldEntity(
                thing.playerName() != null ? ("player_" + thing.playerName()) : pluginName + "_" + thing.getId(),
                bodyType.getValue(),
                getWorld(),
                thing.getX(),
                thing.getY(),
                thing.getWidth(),
                thing.getHeight(),
                thing.getDensity(),
                thing.getFriction(),
                thing.getRestitution(),
                thing.getAngle(),
                pluginName + "/" + CLIENT_IMAGE_DIRECTORY + thing.getSpriteName()
        ).setDeathTick(thing.getDeathTick()).setHealth(thing.getHealth());

        if(thing.getLinearDampening() != null) {
            worldEntity.getBody().setLinearDamping(thing.getLinearDampening());
        }

        return worldEntity;
    }
}
