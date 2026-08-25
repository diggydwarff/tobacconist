package com.diggydwarff.tobacconistmod.compat.create;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Applies nearby Create fan airflow to Tobacconist smoke particles. */
public final class CreateSmokeClearingCompat {
    private static final int SEARCH_RADIUS = 6;
    private static final double MAX_LATERAL_DISTANCE = 3.25D;
    private static final long CACHE_TICKS = 2L;

    // Cache nearby airflow briefly; weak level keys release entries when worlds unload.
    private static final Map<Level, Map<Long, CachedAirflow>> CACHE = new WeakHashMap<>();

    private CreateSmokeClearingCompat() {}

    public static void register() {
        CreateCompat.installSmokeAirflowResolver(CreateSmokeClearingCompat::getAirflow);
    }

    private static CreateCompat.SmokeAirflow getAirflow(Level level, Vec3 smokePos) {
        if (level == null || smokePos == null) return CreateCompat.SmokeAirflow.NONE;

        BlockPos origin = BlockPos.containing(smokePos);
        long now = level.getGameTime();
        Map<Long, CachedAirflow> levelCache = CACHE.computeIfAbsent(level, ignored -> new HashMap<>());
        long cacheKey = origin.asLong();
        CachedAirflow cached = levelCache.get(cacheKey);
        if (cached != null && now - cached.gameTime <= CACHE_TICKS) {
            return cached.airflow;
        }

        CreateCompat.SmokeAirflow airflow = scanForAirflow(level, smokePos, origin);
        if (levelCache.size() > 512) levelCache.clear();
        levelCache.put(cacheKey, new CachedAirflow(now, airflow));
        return airflow;
    }

    private static CreateCompat.SmokeAirflow scanForAirflow(Level level, Vec3 smokePos, BlockPos origin) {
        CreateCompat.SmokeAirflow best = CreateCompat.SmokeAirflow.NONE;
        double bestStrength = 0.0D;
        int radiusSquared = SEARCH_RADIUS * SEARCH_RADIUS;

        // Restrict the fan scan to a spherical search radius.
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSquared) continue;

                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!(level.getBlockEntity(pos) instanceof EncasedFanBlockEntity fan)) continue;
                    if (fan.getSpeed() == 0) continue;

                    AirCurrent current = fan.getAirCurrent();
                    if (current == null || current.direction == null || current.maxDistance <= 0.0F) continue;

                    Vec3 fanCenter = Vec3.atCenterOf(pos);
                    Direction workingSide = fan.getAirflowOriginSide();
                    if (workingSide == null) continue;

                    // Test smoke against the fan's physical working side; pull airflow reverses travel direction.
                    Vec3 facing = Vec3.atLowerCornerOf(workingSide.getNormal());
                    Vec3 relative = smokePos.subtract(fanCenter);

                    // Accept smoke within the fan's working-side airflow volume.
                    double axialDistance = relative.dot(facing);
                    if (axialDistance < -0.75D || axialDistance > current.maxDistance + 1.50D) continue;

                    Vec3 lateralVector = relative.subtract(facing.scale(axialDistance));
                    double lateralDistance = lateralVector.length();
                    if (lateralDistance > MAX_LATERAL_DISTANCE) continue;

                    double distance = relative.length();
                    double effectiveRange = Math.max(2.0D, current.maxDistance + 2.0D);
                    double proximity = 1.0D - Mth.clamp(distance / effectiveRange, 0.0D, 1.0D);
                    double lateralFactor = 1.0D - Mth.clamp(
                            lateralDistance / MAX_LATERAL_DISTANCE, 0.0D, 1.0D);
                    double speedFactor = Mth.clamp(Math.abs(fan.getSpeed()) / 64.0D, 0.45D, 1.50D);

                    // Scale force by proximity, lateral alignment, and fan speed.
                    double strength = (0.0015D + proximity * 0.0105D)
                            * (0.30D + lateralFactor * 0.70D)
                            * speedFactor;
                    if (strength <= bestStrength) continue;

                    Vec3 flowDirection;
                    if (current.pushing) {
                        // Pushing airflow carries smoke outward.
                        flowDirection = facing;
                    } else {
                        // Pulling airflow converges smoke toward the intake center.
                        Vec3 towardFan = fanCenter.subtract(smokePos);
                        if (towardFan.lengthSqr() < 1.0e-6D) {
                            flowDirection = facing.scale(-1.0D);
                        } else {
                            Vec3 axialPull = facing.scale(-1.0D);
                            flowDirection = towardFan.normalize().scale(0.70D)
                                    .add(axialPull.scale(0.30D))
                                    .normalize();
                        }
                    }

                    // Remove smoke after it reaches the pulling fan's intake volume.
                    boolean intakeCapture = !current.pushing
                            && axialDistance > -0.35D
                            && axialDistance < 1.40D
                            && lateralDistance < 0.95D;

                    bestStrength = strength;
                    best = new CreateCompat.SmokeAirflow(
                            flowDirection.x,
                            flowDirection.y,
                            flowDirection.z,
                            strength,
                            intakeCapture
                    );
                }
            }
        }

        return best;
    }

    private record CachedAirflow(long gameTime, CreateCompat.SmokeAirflow airflow) {}
}
