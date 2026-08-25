package com.diggydwarff.tobacconistmod.compat.create;

import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Create Mixer recipe marker for batch leaf-quality homogenization. */
public final class CreateTobaccoHomogenizingRecipe extends MixingRecipe {
    public static final int DEFAULT_BATCH_SIZE = 64;
    public static final int MIN_BATCH_SIZE = 16;
    public static final int FINISH_SIGNAL = 15;

    public CreateTobaccoHomogenizingRecipe(ProcessingRecipeParams params) {
        super(params);
        // Keep the recipe's single leaf ingredient for Create's recipe lookup. Actual batch
        // counts and the requirement for visible quality variance are enforced by
        // CreateTobaccoHomogenization.
    }

    public ItemStack getLeafTemplate() {
        if (getRollableResults().isEmpty()) return ItemStack.EMPTY;
        return getRollableResults().getFirst().getStack().copyWithCount(1);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreateMixerCompat.TOBACCO_HOMOGENIZING.get();
    }
}
