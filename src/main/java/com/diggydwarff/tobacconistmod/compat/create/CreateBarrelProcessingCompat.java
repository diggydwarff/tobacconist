package com.diggydwarff.tobacconistmod.compat.create;

import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Create equivalents for the physical pressure/low-heat requirements used by tobacco barrels. */
public final class CreateBarrelProcessingCompat {
    private CreateBarrelProcessingCompat() {}

    public static void register() {
        CreateCompat.installBarrelPressureResolver(CreateBarrelProcessingCompat::isRunningPress);
        CreateCompat.installStovingHeatResolver(CreateBarrelProcessingCompat::isStovingBurner);
    }

    private static boolean isRunningPress(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MechanicalPressBlockEntity press && press.getSpeed() != 0;
    }

    private static boolean isStovingBurner(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlazeBurnerBlock) || !state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return false;
        }
        HeatLevel heat = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
        return heat == HeatLevel.KINDLED || heat == HeatLevel.SEETHING;
    }
}
