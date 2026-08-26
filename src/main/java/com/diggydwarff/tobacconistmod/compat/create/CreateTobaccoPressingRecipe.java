package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraft.world.level.Level;

/**
 * Create pressing recipe with output derived from the runtime input ItemStack.
 *
 * <p>Create pressing JSON uses a static output, while Tobacconist must preserve per-stack
 * quality, cure, fermentation, age, and processing metadata. Matching Rough tobacco produces
 * a one-item Flake result copied from the runtime input.</p>
 */
public final class CreateTobaccoPressingRecipe extends PressingRecipe {
    public CreateTobaccoPressingRecipe(ProcessingRecipeParams params) {
        super(params);

        // Mark the representative recipe output as Flake for recipe viewers. Runtime processing
        // replaces it with a metadata-preserving copy of the input.
        if (!results.isEmpty()) {
            ProcessingOutput displayOutput = results.get(0);
            ItemStack displayStack = displayOutput.getStack();
            TobaccoCuringHelper.setCutType(displayStack, TobaccoCuringHelper.CUT_FLAKE);
            results.set(0, new ProcessingOutput(displayStack, displayOutput.getChance()));
        }
    }

    @Override
    public boolean matches(RecipeWrapper input, Level level) {
        if (!super.matches(input, level)) {
            return false;
        }

        ItemStack stack = input.getItem(0);
        if (!TobaccoProcessingHelper.canMechanicallyPressToFlake(stack)) {
            return false;
        }

        ItemStack result = TobaccoProcessingHelper.mechanicallyPressOne(stack);
        if (result.isEmpty()) {
            return false;
        }

        // Recipe instances are shared, so capture an independent one-item result and return a
        // fresh copy whenever Create rolls this processing result.
        ItemStack resultTemplate = result.copy();
        resultTemplate.setCount(1);
        enforceNextResult(resultTemplate::copy);
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreatePressCompat.TOBACCO_PRESSING.get();
    }
}
