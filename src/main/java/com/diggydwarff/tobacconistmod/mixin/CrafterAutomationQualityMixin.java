package com.diggydwarff.tobacconistmod.mixin;

import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Distinguishes the Vanilla Crafter from genuine hand crafting without changing recipe previews.
 * The helper itself is a no-op unless Create is installed and the configured penalty is non-zero.
 */
@Mixin(CrafterBlock.class)
public abstract class CrafterAutomationQualityMixin {
    @Inject(method = "dispenseItem", at = @At("HEAD"))
    private void tobacconist$penalizeGenericCrafter(ServerLevel level,
                                                     BlockPos pos,
                                                     CrafterBlockEntity crafter,
                                                     ItemStack stack,
                                                     BlockState state,
                                                     RecipeHolder<CraftingRecipe> recipe,
                                                     CallbackInfo ci) {
        TobaccoProductQualityHelper.applyGenericAutomationPenalty(stack);
    }
}
