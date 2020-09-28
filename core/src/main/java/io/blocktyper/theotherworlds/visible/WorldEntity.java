package io.blocktyper.theotherworlds.visible;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import io.blocktyper.theotherworlds.chunk.Chunk;

import java.util.Optional;

public class WorldEntity {
    private String id;
    private Optional<Chunk> chunk;
    private Visual visual;
    private Body body;
    private Fixture fixture;

    public static final double DEGREES_TO_RADIANS = (Math.PI / 180);
    public static final double RADIANS_TO_DEGREES = (180 / Math.PI);

    public WorldEntity(String id, Visual visual, Body body, Fixture fixture) {
        this.id = id;
        this.visual = visual;
        this.body = body;
        this.fixture = fixture;
        this.chunk = Optional.empty();
    }

    public WorldEntity(Sprite sprite, Body body, Fixture fixture, Optional<RelativeState> relativeState) {
        this.visual = new Visual(sprite, null);
        this.body = body;
        this.fixture = fixture;
        this.chunk = Optional.empty();
    }

    public Vector3 getVector() {
        return new Vector3(body.getPosition().x, body.getPosition().y, 0f);
    }

    public Optional<Chunk> getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = Optional.of(chunk);
    }

    public Body getBody() {
        return body;
    }

    public Sprite getSprite() {
        return visual.getSprite(1);
    }

    public Visual getVisual() {
        return visual;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public static WorldEntity box(Sprite sprite, World world, float density, float friction, float restitution) {
        return box(null, sprite, world, density, friction, restitution);
    }

    public static WorldEntity box(String id, Sprite sprite, World world, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(sprite.getX(), sprite.getY());
        Body body = world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(sprite.getWidth() / 2, sprite.getHeight() / 2);

        float angle = (float) (sprite.getRotation() * DEGREES_TO_RADIANS);
        body.setTransform(body.getWorldCenter(), angle);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution; // bounciness

        Fixture fixture = body.createFixture(fixtureDef);
        box.dispose();

        return new WorldEntity(id, new Visual(sprite, null), body, fixture);
    }
}
