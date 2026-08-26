package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.compat.create.CreateTobaccoHomogenization;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Refreshes Tobacconist homogenization matching when Basin analog control changes. */
@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinHomogenizationStateMixin {
    @Unique
    private boolean tobacconist$signalInitialized;
    @Unique
    private int tobacconist$lastSignal;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tobacconist$refreshOnSignalChange(CallbackInfo ci) {
        BasinBlockEntity basin = (BasinBlockEntity) (Object) this;
        Level level = basin.getLevel();
        if (level == null || level.isClientSide) return;

        int signal = CreateTobaccoHomogenization.getControlSignal(basin);
        if (!tobacconist$signalInitialized) {
            tobacconist$signalInitialized = true;
            tobacconist$lastSignal = signal;
            return;
        }

        // Skip all Tobacconist work for unrelated idle Basins. We still monitor any
        // non-zero signal so changing an Analog Lever remains immediately responsive.
        if (signal == 0 && tobacconist$lastSignal == 0
                && !CreateTobaccoHomogenization.shouldMonitor(basin)) {
            return;
        }

        if (CreateTobaccoHomogenization.hasActiveBatch(basin)) {
            CreateTobaccoHomogenization.clearIfOperatorInactive(basin);
        }
        CreateTobaccoHomogenization.servicePendingCommands(basin);

        if (signal == tobacconist$lastSignal) return;

        int previous = tobacconist$lastSignal;
        tobacconist$lastSignal = signal;
        CreateTobaccoHomogenization.onControlSignalChanged(basin, previous, signal);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void tobacconist$clearHomogenizationSnapshot(CallbackInfo ci) {
        CreateTobaccoHomogenization.clear((BasinBlockEntity) (Object) this);
    }
}
