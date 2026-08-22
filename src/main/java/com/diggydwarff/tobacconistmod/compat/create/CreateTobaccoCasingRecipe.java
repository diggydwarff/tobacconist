package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Dynamic Spout recipe that preserves the exact loose-tobacco metadata while applying one full essence bottle. */
public final class CreateTobaccoCasingRecipe extends FillingRecipe {
    private static final int ESSENCE_DOSE = 1000;
    private final BottledMolassesFlavors flavor;

    public CreateTobaccoCasingRecipe(ProcessingRecipeParams params) {
        super(params);
        this.flavor = resolveFlavor();

        if (!ingredients.isEmpty()) {
            NonNullList<Ingredient> runtimeIngredients = NonNullList.create();
            runtimeIngredients.add(new CreateTobaccoCaptureIngredient(
                    this::canUseTobacco,
                    this::captureTobacco
            ).toVanilla());
            this.ingredients = runtimeIngredients;
        }
    }

    private BottledMolassesFlavors resolveFlavor() {
        if (fluidIngredients.isEmpty()) return null;
        SizedFluidIngredient ingredient = fluidIngredients.getFirst();
        for (BottledMolassesFlavors candidate : BottledMolassesFlavors.values()) {
            if (candidate.isPlain()) continue;
            FluidStack probe = new FluidStack(ModExtractionFluids.essence(candidate), ESSENCE_DOSE);
            if (ingredient.test(probe)) return candidate;
        }
        return null;
    }

    private boolean canUseTobacco(ItemStack tobacco) {
        return flavor != null && TobaccoAromaticHelper.canAromatize(tobacco);
    }

    private void captureTobacco(ItemStack tobacco) {
        if (!canUseTobacco(tobacco)) return;
        ItemStack result = TobaccoAromaticHelper.aromatize(tobacco, flavor);
        if (result.isEmpty()) return;
        ItemStack template = result.copy();
        template.setCount(1);
        enforceNextResult(template::copy);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreateFillingCompat.TOBACCO_CASING.get();
    }
}
