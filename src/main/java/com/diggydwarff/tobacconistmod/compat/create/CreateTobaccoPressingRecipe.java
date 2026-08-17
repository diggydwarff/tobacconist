package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Create pressing recipe whose real output is derived from the actual input ItemStack.
 *
 * <p>A normal Create pressing JSON has a static output. Tobacconist cannot use that directly
 * because quality, cure, fermentation, age and the rest of the tobacco processing state live on
 * the individual stack. When this recipe matches Rough tobacco, it supplies Create with a
 * one-item Flake result copied from that exact input. Create then applies that result once per
 * input item, preserving quantities without multiplying them.</p>
 */
public final class CreateTobaccoPressingRecipe extends PressingRecipe {
    public CreateTobaccoPressingRecipe(ProcessingRecipeParams params) {
        super(params);

        // Make the representative recipe output display as Flake in recipe viewers. The actual
        // processing result is replaced in matches() with a metadata-preserving copy of the input.
        if (!results.isEmpty()) {
            ProcessingOutput displayOutput = results.getFirst();
            ItemStack displayStack = displayOutput.getStack();
            TobaccoCuringHelper.setCutType(displayStack, TobaccoCuringHelper.CUT_FLAKE);
            results.set(0, new ProcessingOutput(displayStack, displayOutput.getChance()));
        }
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
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
