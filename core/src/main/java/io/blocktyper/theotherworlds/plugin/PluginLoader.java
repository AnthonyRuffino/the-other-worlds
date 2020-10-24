package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import io.blocktyper.theotherworlds.plugin.entities.Thing;
import io.blocktyper.theotherworlds.server.world.WorldEntity;

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

    default WorldEntity thingToWorldEntity(Thing thing, String pluginName, BodyDef.BodyType bodyType) {
        return new WorldEntity(
                pluginName + "_" + thing.getId(),
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
    }
}
