package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata-preserving Create Mixer recipe for 2- or 3-variety loose tobacco blends.
 * Each ingredient captures the runtime stack selected from the Basin; the result is built from
 * those stacks so quality, cure, age, and processing state match crafting-grid output.
 */
public final class CreateTobaccoBlendingRecipe extends MixingRecipe {
    private final int blendSize;
    private final List<ItemStack> captured = new ArrayList<>();

    public CreateTobaccoBlendingRecipe(ProcessingRecipeParams params) {
        super(params);
        blendSize = ingredients.size();

        if (blendSize == 2 || blendSize == 3) {
            NonNullList<Ingredient> runtimeIngredients = NonNullList.create();
            for (int i = 0; i < blendSize; i++) {
                final int index = i;
                runtimeIngredients.add(new CreateTobaccoBlendIngredient(
                        stack -> canCapture(index, stack),
                        stack -> capture(index, stack)
                ).toVanilla());
            }
            this.ingredients = runtimeIngredients;
        }
    }

    private boolean canCapture(int index, ItemStack stack) {
        if (!TobaccoBlendHelper.isBlendableBaseTobacco(stack)) return false;

        List<ItemStack> trial = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            if (i >= captured.size() || captured.get(i).isEmpty()) return false;
            trial.add(captured.get(i));
        }
        trial.add(stack);

        // One stack is always valid on its own. From the second capture onward, enforce the same
        // compatibility rules used by the crafting-grid recipe.
        return trial.size() == 1 || TobaccoBlendHelper.canBlend(trial);
    }

    private void capture(int index, ItemStack stack) {
        while (captured.size() <= index) captured.add(ItemStack.EMPTY);
        ItemStack copy = stack.copy();
        copy.setCount(1);
        captured.set(index, copy);

        if (index != blendSize - 1) return;

        List<ItemStack> inputs = new ArrayList<>(captured.subList(0, blendSize));
        ItemStack result = TobaccoBlendHelper.blend(inputs);
        if (result.isEmpty()) return;

        ItemStack template = result.copy();
        enforceNextResult(template::copy);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreateMixerCompat.TOBACCO_BLENDING.get();
    }
}
