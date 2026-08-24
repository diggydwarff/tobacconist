package com.diggydwarff.tobacconistmod.compat.create;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Create-side resolver for fan-assisted Drying Rack curing.
 *
 * <p>The Drying Rack remains the processing machine. This class only reports what kind of
 * Create airflow is actually reaching the rack, allowing the rack's existing time, quality,
 * interruption, rain-damage, and mixed-method systems to remain authoritative.</p>
 */
public final class CreateFanCuringCompat {

    private CreateFanCuringCompat() {}

    public static void register() {
        CreateCompat.installFanCuringResolver(CreateFanCuringCompat::resolveAssist);
    }

    private static CreateCompat.FanCuringAssist resolveAssist(Level level, BlockPos rackPos) {
        if (level == null || rackPos == null) {
            return CreateCompat.FanCuringAssist.NONE;
        }

        // The authored drying rack is ~1.65 blocks tall even though its block entity lives in
        // the lower block position. Probe both vertical sections so a horizontal fan aimed at
        // either the lower shelves OR the upper leaves counts as assisted curing.
        CreateCompat.FanCuringAssist lower = resolveAssistAtProbe(level, rackPos);
        CreateCompat.FanCuringAssist upper = resolveAssistAtProbe(level, rackPos.above());
        return upper.priority() > lower.priority() ? upper : lower;
    }

    private static CreateCompat.FanCuringAssist resolveAssistAtProbe(Level level, BlockPos probePos) {
        int searchDistance = getConfiguredSearchDistance();
        CreateCompat.FanCuringAssist best = CreateCompat.FanCuringAssist.NONE;

        // A fan can only affect this part of the rack when both positions are aligned on one
        // cardinal axis. Search outward from the probe rather than scanning a cube.
        for (Direction fanFacing : Direction.values()) {
            for (int distance = 1; distance <= searchDistance; distance++) {
                BlockPos fanPos = probePos.relative(fanFacing.getOpposite(), distance);
                if (!(level.getBlockEntity(fanPos) instanceof EncasedFanBlockEntity fan)) {
                    continue;
                }

                if (fan.getSpeed() == 0 || fan.getAirflowOriginSide() != fanFacing) {
                    continue;
                }

                AirCurrent current = fan.getAirCurrent();
                if (current == null || current.direction == null || current.maxDistance <= 0) {
                    continue;
                }

                // Create queries belt/depot processing at offset (block distance - 1).
                // Reuse that coordinate so AirCurrent's obstruction/catalyst segments remain
                // authoritative for both the lower and upper rack probes.
                float airflowOffset = distance - 1.0f;
                if (airflowOffset > current.maxDistance + 1.0e-3f) {
                    continue;
                }

                CreateCompat.FanCuringAssist assist = classify(current.getTypeAt(airflowOffset));
                if (assist.priority() > best.priority()) {
                    best = assist;
                }

                if (best == CreateCompat.FanCuringAssist.FIRE) {
                    return best;
                }
            }
        }

        return best;
    }

    private static CreateCompat.FanCuringAssist classify(FanProcessingType processingType) {
        if (processingType == null) {
            return CreateCompat.FanCuringAssist.AIR;
        }
        if (processingType == AllFanProcessingTypes.SMOKING) {
            return CreateCompat.FanCuringAssist.FIRE;
        }
        if (processingType == AllFanProcessingTypes.BLASTING) {
            return CreateCompat.FanCuringAssist.FLUE;
        }

        // Water, soul-fire/haunting, and unknown addon catalysts are not valid curing air.
        return CreateCompat.FanCuringAssist.NONE;
    }

    private static int getConfiguredSearchDistance() {
        double push = AllConfigs.server().kinetics.fanPushDistance.get();
        double pull = AllConfigs.server().kinetics.fanPullDistance.get();
        return Math.max(4, (int) Math.ceil(Math.max(push, pull)) + 2);
    }
}
