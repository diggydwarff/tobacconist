package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.simibubi.create.content.kinetics.crafter.RecipeGridHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the generic automation quality rule to ordinary recipes run by Mechanical Crafters. */
@Mixin(value = RecipeGridHandler.class, remap = false)
public abstract class RecipeGridAutomationQualityMixin {
    @Inject(method = "tryToApplyRecipe", at = @At("RETURN"))
    private static void tobacconist$penalizeMechanicalCrafter(Level level,
                                                               RecipeGridHandler.GroupedItems items,
                                                               CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result != null && !result.isEmpty()) {
            TobaccoProductQualityHelper.applyGenericAutomationPenalty(result);
        }
    }
}
