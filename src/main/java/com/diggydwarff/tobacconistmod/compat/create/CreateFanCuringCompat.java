package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.block.custom.IndustrialDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Create-side resolver for fan-assisted Drying Rack curing.
 *
 * <p>Reports the Create airflow reaching a rack; curing time, quality, interruption,
 * rain damage, and mixed-method state remain owned by the rack block entity.</p>
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

        BlockState rackState = level.getBlockState(rackPos);
        if (rackState.getBlock() instanceof IndustrialDryingRackBlock
                && rackState.hasProperty(TobaccoDryingRackBlock.HALF)
                && rackState.getValue(TobaccoDryingRackBlock.HALF) == DoubleBlockHalf.UPPER) {
            rackPos = rackPos.below();
            rackState = level.getBlockState(rackPos);
        }

        ProbeCandidates lower = resolveCandidatesAtProbe(level, rackPos);
        ProbeCandidates upper = resolveCandidatesAtProbe(level, rackPos.above());

        if (rackState.getBlock() instanceof IndustrialDryingRackBlock) {
            // The factory rack deliberately costs more infrastructure: both tiers need matching
            // airflow from two distinct Encased Fans. One fan reaching both probes is insufficient.
            for (CreateCompat.FanCuringAssist assist : new CreateCompat.FanCuringAssist[]{
                    CreateCompat.FanCuringAssist.FIRE,
                    CreateCompat.FanCuringAssist.FLUE,
                    CreateCompat.FanCuringAssist.AIR}) {
                if (hasDistinctPair(lower.sources(assist), upper.sources(assist))) {
                    return assist;
                }
            }
            return CreateCompat.FanCuringAssist.NONE;
        }

        // Wooden racks keep the lighter requirement: airflow reaching either vertical section is enough.
        CreateCompat.FanCuringAssist lowerBest = lower.best();
        CreateCompat.FanCuringAssist upperBest = upper.best();
        return upperBest.priority() > lowerBest.priority() ? upperBest : lowerBest;
    }

    private static ProbeCandidates resolveCandidatesAtProbe(Level level, BlockPos probePos) {
        int searchDistance = getConfiguredSearchDistance();
        ProbeCandidates candidates = new ProbeCandidates();

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

                float airflowOffset = distance - 1.0f;
                if (airflowOffset > current.maxDistance + 1.0e-3f) {
                    continue;
                }

                CreateCompat.FanCuringAssist assist = classify(current.getTypeAt(airflowOffset));
                if (assist != CreateCompat.FanCuringAssist.NONE) {
                    candidates.add(assist, fanPos);
                }
            }
        }

        return candidates;
    }

    private static boolean hasDistinctPair(Set<BlockPos> lower, Set<BlockPos> upper) {
        for (BlockPos lowerFan : lower) {
            for (BlockPos upperFan : upper) {
                if (!lowerFan.equals(upperFan)) return true;
            }
        }
        return false;
    }

    private static final class ProbeCandidates {
        private final EnumMap<CreateCompat.FanCuringAssist, Set<BlockPos>> sources =
                new EnumMap<>(CreateCompat.FanCuringAssist.class);

        private void add(CreateCompat.FanCuringAssist assist, BlockPos source) {
            sources.computeIfAbsent(assist, ignored -> new HashSet<>()).add(source.immutable());
        }

        private Set<BlockPos> sources(CreateCompat.FanCuringAssist assist) {
            return sources.getOrDefault(assist, Set.of());
        }

        private CreateCompat.FanCuringAssist best() {
            CreateCompat.FanCuringAssist best = CreateCompat.FanCuringAssist.NONE;
            for (CreateCompat.FanCuringAssist assist : sources.keySet()) {
                if (assist.priority() > best.priority()) best = assist;
            }
            return best;
        }
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
