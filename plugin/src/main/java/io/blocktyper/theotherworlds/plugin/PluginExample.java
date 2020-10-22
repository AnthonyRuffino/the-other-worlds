package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.*;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.entities.Damageable;
import io.blocktyper.theotherworlds.plugin.entities.Thing;

import java.util.List;
import java.util.Optional;

public class PluginExample extends BasePlugin {

    private int damageAmount;

    @Override
    public void init(PluginServer pluginServer, JsonNode config) {
        super.init(pluginServer, config);
        damageAmount = config != null ? Optional.ofNullable(config.get("collisionDamage")).map(JsonNode::intValue).orElse(-20) : -20;
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
        long lifeSpan = (5 * 1000)/pluginServer.getTickDelayMillis();
        return Optional.of(() -> {
            long tick = pluginServer.getTick();
            if (tick % 10 == 0) {

                String entityId = "e_" + tick;

                return List.of(new Thing() {
                    @Override
                    public String getId() {
                        return entityId;
                    }

                    @Override
                    public String getSpriteName() {
                        return "sun.jpg";
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
                        return tick;
                    }

                    @Override
                    public float getHeight() {
                        return tick;
                    }

                    @Override
                    public float getDensity() {
                        return tick;
                    }

                    @Override
                    public float getFriction() {
                        return tick;
                    }

                    @Override
                    public float getRestitution() {
                        return tick;
                    }

                    @Override
                    public float getAngle() {
                        return tick;
                    }

                    @Override
                    public Long getDeathTick() {
                        return tick + lifeSpan;
                    }

                    @Override
                    public Integer getHealth() {
                        return 150;
                    }
                });
            }

            return List.of();
        });
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
                return 10000;
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
}
