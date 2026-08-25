package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Pair-wise Create homogenizing recipe for raw or cured tobacco leaves.
 *
 * <p>Each Mixer cycle consumes one leaf from two compatible stacks with different quality
 * values and returns two leaves at their integer average. Raw leaves average GrowthQuality;
 * cured leaves average final Quality. The second ingredient rejects the first captured quality.</p>
 */
public final class CreateTobaccoHomogenizingRecipe extends MixingRecipe {
    private ItemStack firstLeaf = ItemStack.EMPTY;

    public CreateTobaccoHomogenizingRecipe(ProcessingRecipeParams params) {
        super(params);

        if (ingredients.size() >= 2) {
            NonNullList<Ingredient> runtimeIngredients = NonNullList.create();
            runtimeIngredients.add(new CreateTobaccoHomogenizingIngredient(
                    stack -> TobaccoCuringHelper.isRawTobaccoLeaf(stack)
                            || TobaccoCuringHelper.isDryTobaccoLeaf(stack),
                    this::captureFirst
            ).toVanilla());
            runtimeIngredients.add(new CreateTobaccoHomogenizingIngredient(
                    this::canUseSecond,
                    this::captureSecond
            ).toVanilla());
            this.ingredients = runtimeIngredients;
        }
    }

    private void captureFirst(ItemStack stack) {
        firstLeaf = stack.copy();
        firstLeaf.setCount(1);
    }

    private boolean canUseSecond(ItemStack stack) {
        return !firstLeaf.isEmpty()
                && TobaccoProcessingHelper.canMechanicallyHomogenizeLeaves(firstLeaf, stack);
    }

    private void captureSecond(ItemStack stack) {
        if (!canUseSecond(stack)) {
            return;
        }

        ItemStack result = TobaccoProcessingHelper.mechanicallyHomogenizeLeafPair(firstLeaf, stack);
        if (result.isEmpty()) {
            return;
        }

        ItemStack template = result.copy();
        enforceNextResult(template::copy);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreateMixerCompat.TOBACCO_HOMOGENIZING.get();
    }
}
