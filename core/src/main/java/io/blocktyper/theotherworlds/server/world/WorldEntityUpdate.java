package io.blocktyper.theotherworlds.server.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.NumberUtils;
import io.blocktyper.theotherworlds.plugin.entities.WorldEntity;

import java.util.Optional;
import java.util.function.Function;

public class WorldEntityUpdate {


    private String id;
    private Optional<Integer> bodyType;
    private Optional<Float> x;
    private Optional<Float> y;
    private Optional<Float> width;
    private Optional<Float> height;
    private Optional<Vector2> linearVelocity;
    private Optional<Float> angularVelocity;
    private Optional<Float> linearDampening;
    private Optional<Float> angularDampening;
    private Optional<Float> density;
    private Optional<Float> friction;
    private Optional<Float> restitution;
    private Optional<Float> angle;
    private Optional<String> spriteName;
    private Optional<String> xOrientation;


    private WorldEntityUpdate() {

    }

    public WorldEntityUpdate(String id) {
        this.id = id;
    }

    public WorldEntityUpdate(WorldEntity worldEntity) {
        this.id = worldEntity.getId();
        this.bodyType = Optional.of(worldEntity.getBody().getType().getValue());
        this.x = Optional.of(worldEntity.getBody().getPosition().x);
        this.y = Optional.of(worldEntity.getBody().getPosition().y);
        this.width = Optional.of(worldEntity.getWidth());
        this.height = Optional.of(worldEntity.getHeight());
        this.linearVelocity = Optional.of(worldEntity.getBody().getLinearVelocity());
        this.angularVelocity = Optional.of(worldEntity.getBody().getAngularVelocity());
        this.linearDampening = Optional.of(worldEntity.getBody().getLinearDamping());
        this.angularDampening = Optional.of(worldEntity.getBody().getAngularDamping());
        this.density = Optional.of(worldEntity.getFixture().getDensity());
        this.friction = Optional.of(worldEntity.getFixture().getFriction());
        this.restitution = Optional.of(worldEntity.getFixture().getRestitution());
        this.angle = Optional.of(worldEntity.getBody().getAngle());
        this.spriteName = Optional.of(worldEntity.getSpriteName());
        this.xOrientation = Optional.of(worldEntity.getxOrientation());
    }

    private static Optional<Float> getNewState(
            WorldEntityUpdate then,
            WorldEntityUpdate now,
            Function<WorldEntityUpdate, Optional<Float>> accessor
    ) {
        Optional<Float> nowOptional = accessor.apply(now);

        return nowOptional.filter(
                nowValue -> {
                    Optional<Float> thenOptional = accessor.apply(then);
                    return thenOptional.isEmpty() || NumberUtils.floatToIntBits(nowValue) != NumberUtils.floatToIntBits(thenOptional.get());
                }
        );
    }

    private static <T> Optional<T> getNewState(Optional<T> now, Optional<T> then) {
        return now.filter(nowValue -> then.isEmpty() || !then.get().equals(nowValue));
    }

    public static Optional<WorldEntityUpdate> getUpdate(WorldEntityUpdate then, WorldEntityUpdate now) {

        if (!then.id.equals(now.id)) {
            throw new RuntimeException("Wrong comparison between entities: " + then.id + " and " + now.id);
        }

        WorldEntityUpdate update = new WorldEntityUpdate(then.id);

        update.x = getNewState(then, now, WorldEntityUpdate::getX);
        update.y = getNewState(then, now, WorldEntityUpdate::getY);
        update.width = getNewState(then, now, WorldEntityUpdate::getWidth);
        update.height = getNewState(then, now, WorldEntityUpdate::getHeight);

        update.angularVelocity = getNewState(then, now, WorldEntityUpdate::getAngularVelocity);

        update.linearDampening = getNewState(then, now, WorldEntityUpdate::getLinearDampening);
        update.angularDampening = getNewState(then, now, WorldEntityUpdate::getAngularDampening);

        update.density = getNewState(then, now, WorldEntityUpdate::getDensity);
        update.friction = getNewState(then, now, WorldEntityUpdate::getFriction);
        update.restitution = getNewState(then, now, WorldEntityUpdate::getRestitution);
        update.angle = getNewState(then, now, WorldEntityUpdate::getAngle);

        update.spriteName = getNewState(now.spriteName, then.spriteName);
        update.linearVelocity = getNewState(now.linearVelocity, then.linearVelocity);
        update.bodyType = getNewState(now.bodyType, then.bodyType);
        update.xOrientation = getNewState(now.xOrientation, then.xOrientation);


        return Optional.ofNullable(
                (
                        update.x.isPresent()
                                || update.y.isPresent()
                                || update.width.isPresent()
                                || update.height.isPresent()
                                || update.angularVelocity.isPresent()
                                || update.linearDampening.isPresent()
                                || update.angularDampening.isPresent()
                                || update.density.isPresent()
                                || update.friction.isPresent()
                                || update.restitution.isPresent()
                                || update.angle.isPresent()
                                || update.spriteName.isPresent()
                                || update.linearVelocity.isPresent()
                                || update.bodyType.isPresent()
                                || update.xOrientation.isPresent()
                ) ? update : null
        );
    }


    public static void applyUpdate(WorldEntityUpdate update, WorldEntityUpdate existing) {

        if (!update.id.equals(existing.getId())) {
            throw new RuntimeException("Wrong update application for entities: " + update.id + " to " + existing.getId());
        }

        update.x.ifPresent(value -> existing.x = update.x);
        update.y.ifPresent(value -> existing.y = update.y);

        update.width.ifPresent(value -> existing.width = update.width);
        update.height.ifPresent(value -> existing.height = update.height);

        update.angle.ifPresent(value -> existing.angle = update.angle);
        update.spriteName.ifPresent(value -> existing.spriteName = update.spriteName);
        update.xOrientation.ifPresent(value -> existing.xOrientation = update.xOrientation);
    }


    public String getId() {
        return id;
    }

    public Optional<Float> getX() {
        return x;
    }

    public WorldEntityUpdate setX(Optional<Float> x) {
        this.x = x;
        return this;
    }

    public WorldEntityUpdate setX(Float x) {
        this.x = Optional.of(x);
        return this;
    }

    public Optional<Float> getY() {
        return y;
    }

    public WorldEntityUpdate setY(Optional<Float> y) {
        this.y = y;
        return this;
    }

    public WorldEntityUpdate setY(Float y) {
        this.y = Optional.of(y);
        return this;
    }

    public Optional<Float> getWidth() {
        return width;
    }

    public WorldEntityUpdate setWidth(Optional<Float> width) {
        this.width = width;
        return this;
    }

    public WorldEntityUpdate setWidth(Float width) {
        this.width = Optional.of(width);
        return this;
    }

    public Optional<Float> getHeight() {
        return height;
    }

    public WorldEntityUpdate setHeight(Optional<Float> height) {
        this.height = height;
        return this;
    }

    public WorldEntityUpdate setHeight(Float height) {
        this.height = Optional.of(height);
        return this;
    }

    public Optional<Float> getDensity() {
        return density;
    }

    public WorldEntityUpdate setDensity(Optional<Float> density) {
        this.density = density;
        return this;
    }

    public WorldEntityUpdate setDensity(Float density) {
        this.density = Optional.of(density);
        return this;
    }

    public Optional<Float> getFriction() {
        return friction;
    }

    public WorldEntityUpdate setFriction(Optional<Float> friction) {
        this.friction = friction;
        return this;
    }

    public WorldEntityUpdate setFriction(Float friction) {
        this.friction = Optional.of(friction);
        return this;
    }

    public Optional<Float> getRestitution() {
        return restitution;
    }

    public WorldEntityUpdate setRestitution(Optional<Float> restitution) {
        this.restitution = restitution;
        return this;
    }

    public WorldEntityUpdate setRestitution(Float restitution) {
        this.restitution = Optional.of(restitution);
        return this;
    }

    public Optional<Float> getAngle() {
        return angle;
    }

    public WorldEntityUpdate setAngle(Optional<Float> angle) {
        this.angle = angle;
        return this;
    }

    public WorldEntityUpdate setAngle(Float angle) {
        this.angle = Optional.of(angle);
        return this;
    }

    public Optional<String> getSpriteName() {
        return spriteName;
    }

    public WorldEntityUpdate setSpriteName(Optional<String> spriteName) {
        this.spriteName = spriteName;
        return this;
    }

    public WorldEntityUpdate setSpriteName(String spriteName) {
        this.spriteName = Optional.of(spriteName);
        return this;
    }

    public Optional<Vector2> getLinearVelocity() {
        return linearVelocity;
    }

    public WorldEntityUpdate setLinearVelocity(Optional<Vector2> linearVelocity) {
        this.linearVelocity = linearVelocity;
        return this;
    }

    public WorldEntityUpdate setLinearVelocity(Vector2 linearVelocity) {
        this.linearVelocity = Optional.of(linearVelocity);
        return this;
    }

    public Optional<Float> getAngularVelocity() {
        return angularVelocity;
    }

    public WorldEntityUpdate setAngularVelocity(Optional<Float> angularVelocity) {
        this.angularVelocity = angularVelocity;
        return this;
    }

    public WorldEntityUpdate setAngularVelocity(Float angularVelocity) {
        this.angularVelocity = Optional.of(angularVelocity);
        return this;
    }

    public Optional<Float> getLinearDampening() {
        return linearDampening;
    }

    public WorldEntityUpdate setLinearDampening(Optional<Float> linearDampening) {
        this.linearDampening = linearDampening;
        return this;
    }

    public WorldEntityUpdate setLinearDampening(Float linearDampening) {
        this.linearDampening = Optional.of(linearDampening);
        return this;
    }

    public Optional<Float> getAngularDampening() {
        return angularDampening;
    }

    public WorldEntityUpdate setAngularDampening(Optional<Float> angularDampening) {
        this.angularDampening = angularDampening;
        return this;
    }

    public WorldEntityUpdate setAngularDampening(Float angularDampening) {
        this.angularDampening = Optional.of(angularDampening);
        return this;
    }

    public Optional<String> getxOrientation() {
        return xOrientation;
    }

    public void setxOrientation(Optional<String> xOrientation) {
        this.xOrientation = xOrientation;
    }
}
