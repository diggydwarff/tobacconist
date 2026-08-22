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

/**
 * Optional Create integration that lets Encased Fans physically influence Tobacconist smoke.
 *
 * <p>This deliberately uses a forgiving nearby-airflow volume instead of requiring smoke to sit
 * on the exact one-block fan axis. Pulling fans draw nearby smoke toward the fan; pushing fans
 * carry it away from the fan. The effect gets stronger as smoke gets closer.</p>
 */
public final class CreateSmokeClearingCompat {
    private static final int SEARCH_RADIUS = 6;
    private static final double MAX_LATERAL_DISTANCE = 3.25D;
    private static final long CACHE_TICKS = 2L;

    // Hookahs can emit continuously, so avoid rescanning the surrounding blocks every single tick.
    // Weak level keys ensure the cache disappears when a world is unloaded.
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

        // Sphere rather than a full cube: enough to catch a nearby ceiling/wall fan without
        // making every puff scan an unnecessarily large block volume.
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

                    // getAirflowOriginSide() identifies the physical side of the fan where its
                    // working air volume exists. AirCurrent.direction is the direction the air
                    // itself travels, which reverses for a pulling fan. Using current.direction
                    // here made ceiling extractors only see smoke almost directly against the
                    // intake; haze farther along the ceiling fell outside the axial test.
                    Vec3 facing = Vec3.atLowerCornerOf(workingSide.getNormal());
                    Vec3 relative = smokePos.subtract(fanCenter);

                    // Smoke must be on the fan's working side. Keep a small tolerance around the
                    // fan face for collision/ceiling boundaries, while allowing the full Create
                    // airflow distance outward into the room.
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

                    // This value is applied every particle tick. Far/off-axis smoke only starts
                    // drifting toward the air current; close smoke is progressively grabbed.
                    double strength = (0.0015D + proximity * 0.0105D)
                            * (0.30D + lateralFactor * 0.70D)
                            * speedFactor;
                    if (strength <= bestStrength) continue;

                    Vec3 flowDirection;
                    if (current.pushing) {
                        // Blowing fan: carry smoke outward along the fan face.
                        flowDirection = facing;
                    } else {
                        // Pulling fan: converge toward the fan center as well as along its intake
                        // direction so nearby off-axis haze looks like it is being sucked in.
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

                    // Once smoke has visibly converged on a pulling fan's intake face, consume
                    // it instead of letting vanilla block collision leave a permanent wisp parked
                    // under/against the fan. Use a slightly wider intake funnel than the physical
                    // fan face so ceiling smoke does not form a collision ring around the casing.
                    // Smoke
                    // sliding along a ceiling should visibly converge and then disappear into
                    // the extractor instead of forming a collision ring around its casing.
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
