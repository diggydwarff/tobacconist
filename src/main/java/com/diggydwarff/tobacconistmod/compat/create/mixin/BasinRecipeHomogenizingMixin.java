package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.compat.create.CreateTobaccoHomogenization;
import com.diggydwarff.tobacconistmod.compat.create.CreateTobaccoHomogenizingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies Tobacconist's count-aware homogenization rules to Create Basins. */
@Mixin(value = BasinRecipe.class, remap = false)
public abstract class BasinRecipeHomogenizingMixin {
    @Inject(
            method = "match(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tobacconist$matchHomogenizingBatch(BasinBlockEntity basin,
                                                            Recipe<?> recipe,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (!(recipe instanceof CreateTobaccoHomogenizingRecipe homogenizing)) {
            return;
        }

        FilteringBehaviour filter = basin.getFilter();
        if (filter == null || !filter.test(recipe.getResultItem(basin.getLevel().registryAccess()))) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(CreateTobaccoHomogenization.apply(basin, homogenizing, true));
    }

    @Inject(
            method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tobacconist$applyHomogenizingBatch(BasinBlockEntity basin,
                                                            Recipe<?> recipe,
                                                            boolean test,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (recipe instanceof CreateTobaccoHomogenizingRecipe homogenizing) {
            cir.setReturnValue(CreateTobaccoHomogenization.apply(basin, homogenizing, test));
        }
    }
}
