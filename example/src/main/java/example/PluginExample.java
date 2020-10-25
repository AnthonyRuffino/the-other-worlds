package example;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.BasePlugin;
import io.blocktyper.theotherworlds.plugin.EntityCreator;
import io.blocktyper.theotherworlds.plugin.PluginServer;
import io.blocktyper.theotherworlds.plugin.actions.ActionListener;
import io.blocktyper.theotherworlds.plugin.actions.PlayerAction;
import io.blocktyper.theotherworlds.plugin.actions.PlayerConnectionListener;
import io.blocktyper.theotherworlds.plugin.entities.Damageable;
import io.blocktyper.theotherworlds.plugin.entities.Thing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PluginExample extends BasePlugin implements ActionListener, PlayerConnectionListener {

    private int damageAmount;
    long lifeSpan = 1000;

    private Set<String> INTERESTS = Set.of("forward", "left", "back", "right");

    private Map<String, Thing> thingsToAdd = new ConcurrentHashMap<>();

    @Override
    public void init(String pluginName, PluginServer pluginServer, JsonNode config) {
        super.init(pluginName, pluginServer, config);

        damageAmount = config != null ? Optional.ofNullable(config.get("collisionDamage")).map(JsonNode::intValue).orElse(-20) : -20;
        lifeSpan = (config != null ? Optional.ofNullable(config.get("lifeSpan")).map(JsonNode::intValue).orElse(1000) : 1000) / pluginServer.getTickDelayMillis();
    }

    @Override
    public List<ContactListener> getContactListeners() {
        return List.of(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object entityA = contact.getFixtureA().getBody().getUserData();
                Object entityB = contact.getFixtureB().getBody().getUserData();

                if (entityA instanceof Damageable) {
                    ((Damageable) entityA).changeHealth(damageAmount);
                }
                if (entityA instanceof Damageable) {
                    ((Damageable) entityB).changeHealth(damageAmount);
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    @Override
    public Optional<EntityCreator> getEntityCreator() {
        return Optional.of(() -> {
            long tick = pluginServer.getTick();
            if (tick % 500 == 0) {

                final String entityId;
                final Integer health;
                final Long deathTick;
                final String img;
                if (tick == 10000) {
                    img = "morgan-blocksky.png";
                    health = null;
                    deathTick = null;
                    entityId = "player_a";
                } else {
                    img = "sun.jpg";
                    health = 150;
                    deathTick = tick + lifeSpan;
                    entityId = "e_" + tick;
                }

                return List.of(getThing(tick, entityId, health, tick, deathTick, img));
            }
            if (!thingsToAdd.isEmpty()) {
                synchronized (thingsToAdd) {
                    var l = new ArrayList<>(thingsToAdd.values());
                    thingsToAdd.clear();
                    return l;
                }
            }

            return List.of();
        });
    }

    public Thing getThing(long size, String entityId, Integer health, float restitution, Long deathTick, String img) {
        return new Thing() {
            @Override
            public String getId() {
                return entityId;
            }

            @Override
            public String getSpriteName() {
                return img;
            }

            @Override
            public float getX() {
                return 0;
            }

            @Override
            public float getY() {
                return 1000;
            }

            @Override
            public float getWidth() {
                return size;
            }

            @Override
            public float getHeight() {
                return size;
            }

            @Override
            public float getDensity() {
                return size;
            }

            @Override
            public float getFriction() {
                return size;
            }

            @Override
            public float getRestitution() {
                return restitution;
            }

            @Override
            public float getAngle() {
                return size;
            }

            @Override
            public Long getDeathTick() {
                return deathTick;
            }

            @Override
            public Integer getHealth() {
                return health;
            }
        };
    }

    @Override
    public List<Thing> getStaticThings() {
        return List.of(new Thing() {
            @Override
            public String getId() {
                return "floor_01";
            }

            @Override
            public String getSpriteName() {
                return "sun.jpg";
            }

            @Override
            public float getX() {
                return -1000;
            }

            @Override
            public float getY() {
                return -2000;
            }

            @Override
            public float getWidth() {
                return 100000;
            }

            @Override
            public float getHeight() {
                return 500;
            }

            @Override
            public float getDensity() {
                return 0;
            }

            @Override
            public float getFriction() {
                return 0;
            }

            @Override
            public float getRestitution() {
                return 0;
            }

            @Override
            public float getAngle() {
                return 0;
            }
        });
    }

    @Override
    public Optional<ActionListener> getActionListener() {
        return Optional.of(this);
    }

    @Override
    public Optional<PlayerConnectionListener> getPlayerConnectionListener() {
        return Optional.of(this);
    }

    @Override
    public Set<String> getInterests() {
        return INTERESTS;
    }

    @Override
    public void process(List<PlayerAction> actions) {
        actions.forEach(action ->
                Optional.ofNullable(pluginServer.getDynamicEntities().get("example_" + action.player))
                        .ifPresent(p -> {
                            if ("forward".equals(action.actionName)) {
                                p.getBody().setLinearVelocity(new Vector2(0, 10000));
                            } else if ("left".equals(action.actionName)) {
                                p.getBody().setLinearVelocity(new Vector2(-1000, 0));
                            } else if ("back".equals(action.actionName)) {
                                p.getBody().setLinearVelocity(new Vector2(0, -10000));
                            } else if ("right".equals(action.actionName)) {
                                p.getBody().setLinearVelocity(new Vector2(1000, 0));
                            }
                        })
        );
    }

    @Override
    public void handlePlayerConnection(String player, boolean isDisconnect) {
        if (isDisconnect) {

        } else {
            thingsToAdd.put(player, getThing(2000, player, null, 0f,null, player.equals("b") ? "morgan-blocksky.png" : "mo.png"));
        }
    }
}
