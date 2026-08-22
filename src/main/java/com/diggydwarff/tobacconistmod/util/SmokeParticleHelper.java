package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared smoke behavior for lit smoking items and hookahs.
 *
 * <p>Smoke rises with a little drift. If a solid ceiling is nearby, an extra slow-moving smoke
 * particle is emitted just below it so repeated indoor smoking gradually looks hazier. The
 * particles retain vanilla campfire-smoke rendering while Create Encased Fans can continuously
 * bend them toward or away from nearby airflow.</p>
 */
public final class SmokeParticleHelper {
    private static final int CEILING_SCAN_HEIGHT = 8;

    private SmokeParticleHelper() {}

    public static void spawnClientMouthSmoke(Level level,
                                             double x, double y, double z,
                                             double dirX, double dirZ) {
        if (level == null || !level.isClientSide) return;

        RandomSource random = level.random;
        spawnClientRisingSmoke(level, random, x, y, z, dirX, dirZ, true);
        spawnClientCeilingHaze(level, random, x, y, z);
    }

    public static void spawnServerMouthSmoke(ServerLevel level,
                                             double x, double y, double z,
                                             double dirX, double dirZ) {
        if (level == null) return;

        RandomSource random = level.random;
        // A puff gets a couple of wisps. Exact velocity is sent with particle count 0.
        spawnServerRisingSmoke(level, random, x, y, z, dirX, dirZ, true);
        spawnServerRisingSmoke(level, random, x, y, z, dirX, dirZ, true);
        spawnServerCeilingHaze(level, random, x, y, z, 0.65F);
    }

    public static void spawnServerHookahSmoke(ServerLevel level,
                                              double x, double y, double z) {
        if (level == null) return;

        RandomSource random = level.random;
        spawnServerRisingSmoke(level, random, x, y, z, 0.0D, 0.0D, false);
        // Hookahs tick continuously, so haze is intentionally probabilistic to keep particle
        // counts bounded while still making an enclosed lounge accumulate visible smoke.
        spawnServerCeilingHaze(level, random, x, y, z, 0.18F);
    }

    private static void spawnClientRisingSmoke(Level level,
                                               RandomSource random,
                                               double x, double y, double z,
                                               double dirX, double dirZ,
                                               boolean fromMouth) {
        double px = x + (random.nextDouble() - 0.5D) * 0.05D;
        double py = y + random.nextDouble() * 0.03D;
        double pz = z + (random.nextDouble() - 0.5D) * 0.05D;

        double forward = fromMouth ? 0.012D : 0.003D;
        double vx = dirX * forward + (random.nextDouble() - 0.5D) * 0.005D;
        double vy = (fromMouth ? 0.030D : 0.038D) + random.nextDouble() * 0.012D;
        double vz = dirZ * forward + (random.nextDouble() - 0.5D) * 0.005D;

        level.addParticle(ModParticles.TOBACCO_SMOKE.get(), px, py, pz, vx, vy, vz);
    }

    private static void spawnServerRisingSmoke(ServerLevel level,
                                               RandomSource random,
                                               double x, double y, double z,
                                               double dirX, double dirZ,
                                               boolean fromMouth) {
        double px = x + (random.nextDouble() - 0.5D) * 0.05D;
        double py = y + random.nextDouble() * 0.03D;
        double pz = z + (random.nextDouble() - 0.5D) * 0.05D;

        double forward = fromMouth ? 0.012D : 0.003D;
        double vx = dirX * forward + (random.nextDouble() - 0.5D) * 0.005D;
        double vy = (fromMouth ? 0.030D : 0.038D) + random.nextDouble() * 0.012D;
        double vz = dirZ * forward + (random.nextDouble() - 0.5D) * 0.005D;

        sendSingleWithVelocity(level, ModParticles.TOBACCO_SMOKE.get(),
                px, py, pz, vx, vy, vz);
    }

    private static void spawnClientCeilingHaze(Level level,
                                               RandomSource random,
                                               double x, double y, double z) {
        double hazeY = findCeilingUndersideY(level, BlockPos.containing(x, y, z));
        if (Double.isNaN(hazeY)) return;
        double px = x + (random.nextDouble() - 0.5D) * 0.45D;
        double pz = z + (random.nextDouble() - 0.5D) * 0.45D;
        double vx = (random.nextDouble() - 0.5D) * 0.012D;
        double vy = 0.001D + random.nextDouble() * 0.002D;
        double vz = (random.nextDouble() - 0.5D) * 0.012D;

        // Signal-style campfire smoke is intentionally used here because it persists much longer
        // than ordinary smoke while retaining the vanilla campfire-smoke look.
        level.addParticle(ModParticles.TOBACCO_INDOOR_SMOKE.get(),
                px, hazeY, pz, vx, vy, vz);
    }

    private static void spawnServerCeilingHaze(ServerLevel level,
                                               RandomSource random,
                                               double x, double y, double z,
                                               float chance) {
        if (random.nextFloat() >= chance) return;

        double hazeY = findCeilingUndersideY(level, BlockPos.containing(x, y, z));
        if (Double.isNaN(hazeY)) return;
        double px = x + (random.nextDouble() - 0.5D) * 0.45D;
        double pz = z + (random.nextDouble() - 0.5D) * 0.45D;
        double vx = (random.nextDouble() - 0.5D) * 0.012D;
        double vy = 0.001D + random.nextDouble() * 0.002D;
        double vz = (random.nextDouble() - 0.5D) * 0.012D;

        sendSingleWithVelocity(level, ModParticles.TOBACCO_INDOOR_SMOKE.get(),
                px, hazeY, pz, vx, vy, vz);
    }

    private static double findCeilingUndersideY(Level level, BlockPos origin) {
        for (int distance = 1; distance <= CEILING_SCAN_HEIGHT; distance++) {
            BlockPos scanPos = origin.above(distance);
            BlockState state = level.getBlockState(scanPos);
            if (!state.isAir() && !state.getCollisionShape(level, scanPos).isEmpty()) {
                // The old calculation added the integer block distance to the particle's
                // fractional Y coordinate. In a low room that could place indoor haze *inside*
                // the ceiling block (for example y=1.5 + distance 2 -> y=3.28), leaving it
                // collision-pinned where a ceiling extractor could not slide it laterally.
                // Anchor to the actual lower face of the ceiling block instead.
                return scanPos.getY() - 0.08D;
            }
        }
        return Double.NaN;
    }

    private static void sendSingleWithVelocity(ServerLevel level,
                                               ParticleOptions particle,
                                               double x, double y, double z,
                                               double vx, double vy, double vz) {
        level.sendParticles(particle, x, y, z, 0, vx, vy, vz, 1.0D);
    }
}
