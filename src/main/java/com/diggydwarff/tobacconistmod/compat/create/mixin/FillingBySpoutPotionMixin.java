package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.compat.create.CreatePotionInfusion;
import com.diggydwarff.tobacconistmod.util.PotionInfusion;
import com.simibubi.create.content.fluids.spout.FillingBySpout;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional Create-only hooks avoid baking arbitrary potion metadata into static recipe outputs. */
@Mixin(value = FillingBySpout.class, remap = false)
public abstract class FillingBySpoutPotionMixin {
    @Inject(method = "canItemBeFilled", at = @At("HEAD"), cancellable = true)
    private static void tobacconist$canInfuse(Level level, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PotionInfusion.canTreat(stack)) cir.setReturnValue(true);
    }
    @Inject(method = "getRequiredAmountForItem", at = @At("HEAD"), cancellable = true)
    private static void tobacconist$infusionAmount(Level level, ItemStack stack, FluidStack fluid, CallbackInfoReturnable<Integer> cir) {
        if (CreatePotionInfusion.matches(stack, fluid)) cir.setReturnValue(PotionInfusion.FLUID_DOSE);
    }
    @Inject(method = "fillItem", at = @At("HEAD"), cancellable = true)
    private static void tobacconist$infuse(Level level, int required, ItemStack stack, FluidStack fluid, CallbackInfoReturnable<ItemStack> cir) {
        if (CreatePotionInfusion.matches(stack, fluid)) cir.setReturnValue(CreatePotionInfusion.fill(required, stack, fluid));
    }
}
