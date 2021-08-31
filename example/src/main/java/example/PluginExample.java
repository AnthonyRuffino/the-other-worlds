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
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PluginExample extends BasePlugin implements ActionListener, PlayerConnectionListener {

    private int damageAmount;
    long lifeSpan = 1000;

    private final Set<String> INTERESTS = Set.of("forward", "left", "back", "right");

    private final Map<String, Thing> thingsToAdd = new ConcurrentHashMap<>();

    @Override
    public void init(String pluginName, PluginServer pluginServer, JsonNode config) {
        super.init(pluginName, pluginServer, config);

        damageAmount = Optional.ofNullable(config != null ? config.get("collisionDamage") : null).map(JsonNode::intValue).orElse(-20);
        lifeSpan = Optional.ofNullable(config != null ? config.get("lifeSpan") : null).map(JsonNode::intValue).orElse(1000) / pluginServer.getTickDelayMillis();
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

            allPlayerActions.forEach(this::processPlayerActions);

            if (tick % 100 == 0) {

                final String entityId;
                final Integer health;
                final Long deathTick;
                final String img;
                if (tick == 10000) {
                    img = "morgan-blocksky.png";
                    health = 1000;
                    deathTick = null;
                    entityId = "player_a";
                } else {
                    img = "sun.jpg";
                    health = (int) (5000 + (tick*2));
                    deathTick = tick + lifeSpan + (tick/2);
                    entityId = "e_" + tick;
                }

                return List.of(getThing(tick, entityId, health, tick, deathTick, img, null, null));
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

    private void processPlayerActions(String playerName, Set<String> actions) {
        Optional.ofNullable(pluginServer.getDynamicEntities().get(playerName))
                .ifPresent(player -> {
                    actions.forEach(action -> processPlayerAction(player, action));
                    if (player.getBody().getLinearVelocity() != null) {
                        player.getBody().setLinearVelocity(normalizeIfNeeded(player.getBody().getLinearVelocity()));
                    }
                });
    }

    private void processPlayerAction(WorldEntity player, String action) {
        Optional.ofNullable(DIRECTION_MAP.get(action)).ifPresent(direction ->
            player.getBody().setLinearVelocity(applyDirectionVector(player, direction))
        );
    }

    public Thing getThing(long size, String entityId, Integer health, float restitution, Long deathTick, String img, String playerName, Float linearDampening) {
        return new Thing() {
            @Override
            public String getId() {
                return entityId;
            }

            @Override
            public String playerName() {
                return playerName;
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
                return 1;
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

            @Override
            public Float getLinearDampening() {
                return linearDampening;
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
            public String playerName() {
                return null;
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
                return 1;
            }

            @Override
            public float getRestitution() {
                return 0;
            }

            @Override
            public float getAngle() {
                return 0;
            }

            @Override
            public Float getLinearDampening() {
                return 1f;
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

    Map<String, Set<String>> allPlayerActions = new HashMap<>();

    @Override
    public void handlePlayerActions(List<PlayerAction> actions) {
        actions.forEach(action ->
                Optional.ofNullable(pluginServer.getDynamicEntities().get("player_" + action.player))
                        .ifPresent(p -> {
                            Set<String> playerActions = allPlayerActions.computeIfAbsent(p.getId(), k -> new HashSet<>());
                            registerMotion(p, action, playerActions);
                        })
        );
    }

    private static final Map<String, Vector2> DIRECTION_MAP = Map.of(
            "right", new Vector2(1000, 0),
            "left", new Vector2(-1000, 0),
            "forward", new Vector2(0, 1000),
            "back", new Vector2(0, -1000)
    );

    private void registerMotion(
            WorldEntity player,
            PlayerAction action,
            Set<String> playerActions) {
        if (action.cancel) {
            playerActions.remove(action.actionName);
        } else {
            playerActions.add(action.actionName);
            Optional.ofNullable(DIRECTION_MAP.get(action.actionName)).ifPresent(direction -> {
                Vector2 newVelocity = normalizeIfNeeded(applyDirectionVector(player, direction));
                if (newVelocity != null) {
                    player.getBody().setLinearVelocity(newVelocity);
                }
            });
        }
    }

    private Vector2 applyDirectionVector(WorldEntity player, Vector2 direction) {
        if (player.getBody().getLinearVelocity() == null) {
            return direction;
        }
        return player.getBody().getLinearVelocity().add(direction);
    }

    private Vector2 normalizeIfNeeded(Vector2 direction) {
        if (direction == null) {
            return null;
        }
        float length = direction.len();
        if (length > 100000000) {
            direction.x /= length;
            direction.y /= length;
        }
        return direction;
    }


    @Override
    public void handlePlayerConnection(String player, boolean isDisconnect) {
        if (isDisconnect) {

        } else {
            thingsToAdd.put(player, getThing(2000, player, null, 0f, null, player.equals("b") ? "morgan-blocksky.png" : "mo.png", player, 0.5f));
        }
    }
}
