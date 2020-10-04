package io.blocktyper.theotherworlds.server.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.NumberUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class WorldEntityUpdate {


    private String id;
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


    private WorldEntityUpdate() {

    }

    public WorldEntityUpdate(String id) {
        this.id = id;
    }

    public WorldEntityUpdate(WorldEntity worldEntity) {
        this.id = worldEntity.getId();
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
    }

    public WorldEntity generateBrandWorldEntity(World world) {
        return new WorldEntity(
                id,
                world,
                x.get(),
                y.get(),
                width.get(),
                height.get(),
                density.get(),
                friction.get(),
                restitution.get(),
                angle.get(),
                spriteName.get()
        );
    }


    public WorldEntity generateWorldEntityFromUpdateAndExisting(WorldEntity entity) {
        return new WorldEntity(
                id,
                entity.getBody().getWorld(),
                x.orElse(entity.getBody().getPosition().x),
                y.orElse(entity.getBody().getPosition().y),
                width.orElse(entity.getWidth()),
                height.orElse(entity.getHeight()),
                density.orElse(entity.getFixture().getDensity()),
                friction.orElse(entity.getFixture().getFriction()),
                restitution.orElse(entity.getFixture().getRestitution()),
                angle.orElse(entity.getAngle()),
                spriteName.orElse(entity.getSpriteName())
        );
    }


    private static Optional<Float> diff(
            WorldEntityUpdate then,
            WorldEntityUpdate now,
            Function<WorldEntityUpdate, Optional<Float>> accessor
    ) {
        return accessor.apply(then).flatMap
                (thenValue -> accessor.apply(now)
                        .map(nowValue -> NumberUtils.floatToIntBits(nowValue) == NumberUtils.floatToIntBits(thenValue) ? null : nowValue)
                        .filter(Objects::nonNull)
                );
    }

    public static Optional<WorldEntityUpdate> getUpdate(WorldEntityUpdate then, WorldEntityUpdate now) {

        if (!then.id.equals(now.id)) {
            throw new RuntimeException("Wrong comparison between entities: " + then.id + " and " + now.id);
        }

        WorldEntityUpdate update = new WorldEntityUpdate(then.id);
        update.x = diff(then, now, WorldEntityUpdate::getX);
        update.y = diff(then, now, WorldEntityUpdate::getY);
        update.width = diff(then, now, WorldEntityUpdate::getWidth);
        update.height = diff(then, now, WorldEntityUpdate::getHeight);

        update.angularVelocity = diff(then, now, WorldEntityUpdate::getAngularVelocity);

        update.linearDampening = diff(then, now, WorldEntityUpdate::getLinearDampening);
        update.angularDampening = diff(then, now, WorldEntityUpdate::getAngularDampening);

        update.density = diff(then, now, WorldEntityUpdate::getDensity);
        update.friction = diff(then, now, WorldEntityUpdate::getFriction);
        update.restitution = diff(then, now, WorldEntityUpdate::getRestitution);
        update.angle = diff(then, now, WorldEntityUpdate::getAngle);

        String nowSpriteName = now.spriteName.get();
        update.spriteName = Optional.ofNullable(then.spriteName.get().equals(nowSpriteName) ? null : nowSpriteName);

        Vector2 linearVelocity = now.linearVelocity.get();
        update.linearVelocity = Optional.ofNullable(then.linearVelocity.get().equals(linearVelocity) ? null : linearVelocity);

        return Optional.ofNullable(
                (
                        update.x.isPresent()
                                || update.y.isPresent()
                                || update.width.isPresent()
                                || update.height.isPresent()
                                || update.density.isPresent()
                                || update.friction.isPresent()
                                || update.restitution.isPresent()
                                || update.angle.isPresent()
                ) ? update : null
        );
    }


    public static WorldEntity applyUpdate(WorldEntityUpdate update, WorldEntity entity) {

        if (!update.id.equals(entity.getId())) {
            throw new RuntimeException("Wrong update application for entities: " + update.id + " to " + entity.getId());
        }

        return update.generateWorldEntityFromUpdateAndExisting(entity);

//        update.x.ifPresent(value -> entity.getBody().getPosition().x = value);
//        update.y.ifPresent(value -> entity.getBody().getPosition().y = value);
//
//        update.width.ifPresent(entity::setWidth);
//        update.height.ifPresent(entity::setHeight);
//
//        update.linearVelocity.ifPresent(entity::setLinearVelocity);
//        update.angularVelocity.ifPresent(entity::setAngularVelocity);
//
//        update.linearDampening.ifPresent(entity::setLinearDampening);
//        update.angularDampening.ifPresent(entity::setAngularDampening);
//
//        update.width.ifPresent(entity::setWidth);
//        update.height.ifPresent(entity::setHeight);
//
//
//        update.density.ifPresent(value -> entity.getFixture().setDensity(value));
//        update.friction.ifPresent(value -> entity.getFixture().setFriction(value));
//        update.restitution.ifPresent(value -> entity.getFixture().setRestitution(value));
//        update.angle.ifPresent(entity::setAngle);
//        update.spriteName.ifPresent(entity::setSpriteName);


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
}
