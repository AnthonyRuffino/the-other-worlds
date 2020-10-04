package io.blocktyper.theotherworlds.server.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class WorldEntity {

    private String id;
    private Body body;
    private Fixture fixture;
    private String spriteName;
    private float width;
    private float height;
    private Vector2 linearVelocity;
    private float angularVelocity;
    private float linearDampening;
    private float angularDampening;
    private float angle;


    public WorldEntity(
            String id,
            World world,
            float x,
            float y,
            float width,
            float height,
            float density,
            float friction,
            float restitution,
            float angle,
            String spriteName
    ) {
        this.id = id;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
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

    public WorldEntity setAngle(float angle) {
        this.angle = angle;
        return this;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public WorldEntity setSpriteName(String spriteName) {
        this.spriteName = spriteName;
        return this;
    }

    public Vector2 getLinearVelocity() {
        return linearVelocity;
    }

    public WorldEntity setLinearVelocity(Vector2 linearVelocity) {
        this.linearVelocity = linearVelocity;
        return this;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public WorldEntity setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
        return this;
    }

    public float getLinearDampening() {
        return linearDampening;
    }

    public WorldEntity setLinearDampening(float linearDampening) {
        this.linearDampening = linearDampening;
        return this;
    }

    public float getAngularDampening() {
        return angularDampening;
    }

    public WorldEntity setAngularDampening(float angularDampening) {
        this.angularDampening = angularDampening;
        return this;
    }
}