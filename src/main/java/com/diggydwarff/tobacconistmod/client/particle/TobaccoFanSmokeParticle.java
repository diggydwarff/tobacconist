package com.diggydwarff.tobacconistmod.client.particle;

import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Vanilla campfire smoke with one extra behavior: nearby Create fan airflow is applied every tick.
 * Rendering, fade and random drift remain the vanilla CampfireSmokeParticle behavior.
 */
public final class TobaccoFanSmokeParticle extends CampfireSmokeParticle {
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
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, signal);
        this.indoorSmoke = signal;

        // Keep enclosed-room haze around a little longer than vanilla signal smoke without
        // changing its appearance or turning it into the much heavier custom haze experiment.
        if (this.indoorSmoke) {
            this.lifetime = Math.max(this.lifetime + 28, (int) Math.round(this.lifetime * 1.28D));
        }

        this.pickSprite(sprites);
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

        // A pulling fan should finish the job: once the smoke has visibly reached the intake,
        // remove it instead of letting vanilla collision leave it hovering around the fan block.
        if (airflow.intakeCapture()) {
            this.remove();
            return;
        }

        // Long-lived ceiling haze should stop behaving like trapped indoor smoke once airflow
        // carries it out from beneath the roof. Without this transition a signal-smoke particle
        // could retain its long indoor lifetime, hit an exterior wall, and sit there while the fan
        // kept pushing horizontally. Sky-exposed smoke regains normal buoyancy and its remaining
        // lifetime is capped to a few seconds so it clears like outdoor smoke.
        if (this.indoorSmoke && isSkyExposed()) {
            releaseToOutdoorSmoke();
        }

        // Vanilla collision keeps smoke below solid ceilings, which is desirable, but an upward
        // extractor can otherwise leave those particles parked in place. This must apply to BOTH
        // the long-lived indoor haze and ordinary rising smoke: ordinary wisps can also reach the
        // roof and become collision-stopped before the nearby fan has time to collect them.
        if (airflow.active() && isBlockedAbove()) {
            slideAlongCeiling(airflow);
        }
    }

    private void applyFanAirflow(CreateCompat.SmokeAirflow airflow) {
        // Ceiling smoke is deliberately a little easier for an extractor fan to grab.
        double acceleration = airflow.strength() * (this.indoorSmoke ? 1.30D : 1.0D);
        this.xd += airflow.x() * acceleration;
        this.yd += airflow.y() * acceleration;
        this.zd += airflow.z() * acceleration;

        // Keep strong/high-RPM fans visibly useful without letting a particle accelerate forever.
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

        // Once vanilla Particle collision marks a smoke wisp as stopped by the ceiling, later
        // horizontal move() calls can remain effectively pinned. Move the ceiling-bound wisp by
        // position instead, but only while the destination itself is open air. This keeps the
        // smoke hugging the underside of the roof while still allowing an extractor to collect
        // it from several blocks away.
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

        // Restore an obvious but still gentle upward drift. If the fan has pushed the wisp into
        // an outside wall, lift it a tiny amount by position as well; this bypasses the same
        // vanilla collision pinning that can occur under ceilings and lets it climb/dissipate.
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
        // Check several short offsets. A vanilla smoke particle may stop a few tenths of a block
        // below the actual underside after collision; checking only y + 0.55 missed some of those
        // wisps and prevented the ceiling-slide code from ever running.
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
