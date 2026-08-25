package com.diggydwarff.tobacconistmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * High-throughput Create-only drying rack. It reuses the traditional rack's curing metadata and
 * quality rules, but doubles batch capacity and refuses to make curing progress without Create
 * fan assistance.
 */
public class IndustrialDryingRackBlockEntity extends TobaccoDryingRackBlockEntity {
    public static final int INDUSTRIAL_MAX_LEAVES = 32;

    public IndustrialDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(), pos, state);
    }

    @Override
    public int getMaxLeaves() {
        return INDUSTRIAL_MAX_LEAVES;
    }

    @Override
    public boolean requiresCreateAssistance() {
        return true;
    }

    @Override
    protected int adjustCreateAssistedTickRate(int baseRate) {
        // +1 curing tick per game tick is intentionally modest: plain fan Air/Sun goes 4 -> 5
        // (~20% less wall-clock time), while heated Fire/Flue goes 6 -> 7 (~14% less time).
        return baseRate + 1;
    }

    @Override
    public int getVisualLoadStage() {
        int count = Math.min(getMaxLeaves(), getLeafCount());
        if (count <= 0) return 0;
        if (count <= 10) return 1;
        if (count <= 21) return 2;
        return 3;
    }
}
