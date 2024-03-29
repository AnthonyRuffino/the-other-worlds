package io.blocktyper.theotherworlds.plugin.entities;

import com.badlogic.gdx.physics.box2d.*;

import java.util.Map;

public class WorldEntity implements Damageable {

    private String id;
    private Body body;
    private Fixture fixture;
    private String spriteName;
    private float width;
    private float height;
    private float angle;
    private String xOrientation = "right";
    private String yOrientation = "up";

    private Long deathTick;
    private Integer health;

    public static Map<Integer, BodyDef.BodyType> BODY_TYPES = Map.of(
            0, BodyDef.BodyType.StaticBody,
            1, BodyDef.BodyType.KinematicBody,
            2, BodyDef.BodyType.DynamicBody
    );


    public WorldEntity(
            String id,
            int bodyType,
            World world,
            float x,
            float y,
            float width,
            float height,
            float density,
            float friction,
            float restitution,
            float angle,
            String spriteName,
            String xOrientation
    ) {
        this.id = id;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BODY_TYPES.get(bodyType);
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(width / 2, height / 2);

        this.width = width;
        this.height = height;

        this.angle = angle;
        float radianAngle = (float) (angle * Math.PI / 180);
        body.setTransform(body.getWorldCenter(), radianAngle);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        this.fixture = body.createFixture(fixtureDef);
        box.dispose();

        this.spriteName = spriteName;
        this.body.setUserData(this);
        this.setxOrientation(xOrientation);
    }


    public String getId() {
        return id;
    }

    public WorldEntity setId(String id) {
        this.id = id;
        return this;
    }

    public Body getBody() {
        return body;
    }

    public WorldEntity setBody(Body body) {
        this.body = body;
        return this;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public WorldEntity setFixture(Fixture fixture) {
        this.fixture = fixture;
        return this;
    }

    public float getWidth() {
        return width;
    }

    public WorldEntity setWidth(float width) {
        this.width = width;
        return this;
    }

    public float getHeight() {
        return height;
    }

    public WorldEntity setHeight(float height) {
        this.height = height;
        return this;
    }

    public float getAngle() {
        return angle;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public WorldEntity setSpriteName(String spriteName) {
        this.spriteName = spriteName;
        return this;
    }

    public Long getDeathTick() {
        return deathTick;
    }

    public WorldEntity setDeathTick(Long deathTick) {
        this.deathTick = deathTick;
        return this;
    }

    public Integer getHealth() {
        return health;
    }

    public Integer changeHealth(int amount) {
        if (health != null) {
            health += amount;
        }
        return health;
    }

    public boolean isDead() {
        return health != null && health <= 0;
    }

    public WorldEntity setHealth(Integer health) {
        this.health = health;
        return this;
    }

    public String getxOrientation() {
        return xOrientation;
    }

    public void setxOrientation(String xOrientation) {
        this.xOrientation = xOrientation;
    }

    public String getyOrientation() {
        return yOrientation;
    }

    public void setyOrientation(String yOrientation) {
        this.yOrientation = yOrientation;
    }
}
