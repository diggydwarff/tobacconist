package com.diggydwarff.tobacconistmod.client.particle;

import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Campfire-style smoke with Create fan airflow applied every tick.
 * The 1.20.1 port reproduces the vanilla drift/fade on TextureSheetParticle because
 * CampfireSmokeParticle's constructor is package-private in this Minecraft version.
 */
public final class TobaccoFanSmokeParticle extends TextureSheetParticle {
    private static final double MAX_FAN_SPEED = 0.12D;
    private static final double MAX_CEILING_SLIDE = 0.045D;
    private static final double CEILING_TEST_OFFSET = 0.55D;
    private static final int OUTDOOR_REMAINING_LIFETIME = 72;
    private static final double OUTDOOR_WALL_RISE = 0.020D;

    private final boolean indoorSmoke;
    private boolean escapedShelter;

    private TobaccoFanSmokeParticle(ClientLevel level,
                                    double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed,
                                    boolean signal,
                                    SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.hasPhysics = false;
        this.friction = 0.96F;
        this.gravity = 3.0E-6F;
        this.xd = xSpeed + (this.random.nextDouble() - 0.5D) * 0.003D;
        this.yd = ySpeed + this.random.nextDouble() * 0.002D;
        this.zd = zSpeed + (this.random.nextDouble() - 0.5D) * 0.003D;
        this.quadSize = 0.5F + this.random.nextFloat() * 0.5F;
        this.lifetime = signal
                ? 280 + this.random.nextInt(50)
                : 80 + this.random.nextInt(50);
        this.indoorSmoke = signal;

        // Extend enclosed smoke lifetime without changing the vanilla particle appearance.
        if (this.indoorSmoke) {
            this.lifetime = Math.max(this.lifetime + 28, (int) Math.round(this.lifetime * 1.28D));
        }

        this.pickSprite(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        CreateCompat.SmokeAirflow airflow = CreateCompat.getSmokeAirflow(
                this.level,
                new Vec3(this.x, this.y, this.z)
        );

        if (airflow.active()) {
            applyFanAirflow(airflow);
        }

        super.tick();

        if (!this.removed && this.age > this.lifetime - 60) {
            this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 60.0F);
        }

        // Remove smoke after it reaches a pulling fan intake.
        if (airflow.intakeCapture()) {
            this.remove();
            return;
        }

        // Convert long-lived indoor smoke to normal outdoor behavior after it reaches open sky.
        if (this.indoorSmoke && isSkyExposed()) {
            releaseToOutdoorSmoke();
        }

        // Slide ceiling-blocked smoke along fan airflow so extractors can collect it.
        if (airflow.active() && isBlockedAbove()) {
            slideAlongCeiling(airflow);
        }
    }

    private void applyFanAirflow(CreateCompat.SmokeAirflow airflow) {
        // Increase extractor capture slightly for ceiling-level smoke.
        double acceleration = airflow.strength() * (this.indoorSmoke ? 1.30D : 1.0D);
        this.xd += airflow.x() * acceleration;
        this.yd += airflow.y() * acceleration;
        this.zd += airflow.z() * acceleration;

        // Cap accumulated fan velocity.
        double speedSquared = this.xd * this.xd + this.yd * this.yd + this.zd * this.zd;
        double maxSquared = MAX_FAN_SPEED * MAX_FAN_SPEED;
        if (speedSquared > maxSquared) {
            double scale = MAX_FAN_SPEED / Math.sqrt(speedSquared);
            this.xd *= scale;
            this.yd *= scale;
            this.zd *= scale;
        }
    }

    private void slideAlongCeiling(CreateCompat.SmokeAirflow airflow) {
        double horizontalLength = Math.sqrt(airflow.x() * airflow.x() + airflow.z() * airflow.z());

        if (horizontalLength < 1.0e-5D) return;

        double slide = Math.min(MAX_CEILING_SLIDE,
                0.004D + airflow.strength() * 3.75D);
        double slideX = airflow.x() / horizontalLength * slide;
        double slideZ = airflow.z() / horizontalLength * slide;

        // Move ceiling-bound smoke directly when vanilla collision has pinned its velocity.
        double nextX = this.x + slideX;
        double nextZ = this.z + slideZ;
        BlockPos destination = BlockPos.containing(nextX, this.y, nextZ);
        BlockState destinationState = this.level.getBlockState(destination);
        if (destinationState.isAir() || destinationState.getCollisionShape(this.level, destination).isEmpty()) {
            this.setPos(nextX, this.y, nextZ);
        }

        this.xd += slideX * 0.20D;
        this.zd += slideZ * 0.20D;
    }


    private boolean isSkyExposed() {
        return this.level.canSeeSky(BlockPos.containing(this.x, this.y + 0.15D, this.z));
    }

    private void releaseToOutdoorSmoke() {
        if (!this.escapedShelter) {
            this.escapedShelter = true;
            this.lifetime = Math.min(this.lifetime, this.age + OUTDOOR_REMAINING_LIFETIME);
        }

        // Restore upward drift outdoors and lift wall-pinned smoke through open space.
        this.yd = Math.max(this.yd, OUTDOOR_WALL_RISE);
        if (isAgainstWall()) {
            double nextY = this.y + 0.012D;
            BlockPos destination = BlockPos.containing(this.x, nextY, this.z);
            BlockState state = this.level.getBlockState(destination);
            if (state.isAir() || state.getCollisionShape(this.level, destination).isEmpty()) {
                this.setPos(this.x, nextY, this.z);
            }
        }
    }

    private boolean isAgainstWall() {
        return isSolidAtHorizontalOffset(0.42D, 0.0D)
                || isSolidAtHorizontalOffset(-0.42D, 0.0D)
                || isSolidAtHorizontalOffset(0.0D, 0.42D)
                || isSolidAtHorizontalOffset(0.0D, -0.42D);
    }

    private boolean isSolidAtHorizontalOffset(double xOffset, double zOffset) {
        BlockPos pos = BlockPos.containing(this.x + xOffset, this.y, this.z + zOffset);
        BlockState state = this.level.getBlockState(pos);
        return !state.isAir() && !state.getCollisionShape(this.level, pos).isEmpty();
    }

    private boolean isBlockedAbove() {
        // Probe multiple offsets because collision can stop smoke below the ceiling surface.
        return isSolidAtOffset(0.14D)
                || isSolidAtOffset(0.34D)
                || isSolidAtOffset(CEILING_TEST_OFFSET);
    }

    private boolean isSolidAtOffset(double yOffset) {
        BlockPos above = BlockPos.containing(this.x, this.y + yOffset, this.z);
        BlockState state = this.level.getBlockState(above);
        return !state.isAir() && !state.getCollisionShape(this.level, above).isEmpty();
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final boolean signal;

        public Provider(SpriteSet sprites, boolean signal) {
            this.sprites = sprites;
            this.signal = signal;
        }

        @Override
        public Particle createParticle(SimpleParticleType type,
                                       ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new TobaccoFanSmokeParticle(
                    level, x, y, z,
                    xSpeed, ySpeed, zSpeed,
                    this.signal,
                    this.sprites
            );
        }
    }
}
