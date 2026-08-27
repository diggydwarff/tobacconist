package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.ModMolassesFluids;
import com.diggydwarff.tobacconistmod.fluid.TobacconistFluidAmounts;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;


/** Dynamic Create mixing recipe: any loose tobacco/unused Shisha + one 250 mB molasses bottle -> Shisha. */
public final class CreateTobaccoMixingRecipe extends MixingRecipe {
    private static final int FLAVOR_PROBE_AMOUNT = TobacconistFluidAmounts.BOTTLE;

    private final BottledMolassesFlavors flavor;
    private ItemStack immediateContinuationGuard = ItemStack.EMPTY;

    public CreateTobaccoMixingRecipe(ProcessingRecipeParams params) {
        super(params);
        this.flavor = resolveFlavor();

        // Keep params.ingredients untouched for serialization/network sync; only replace the
        // runtime ingredient list used by BasinRecipe.apply().
        if (!ingredients.isEmpty()) {
            NonNullList<Ingredient> runtimeIngredients = NonNullList.create();
            for (int i = 0; i < ingredients.size(); i++) {
                runtimeIngredients.add(new CreateTobaccoCaptureIngredient(
                        this::canUseTobacco,
                        this::captureTobacco
                ).toVanilla());
            }
            this.ingredients = runtimeIngredients;
        }
    }

    private BottledMolassesFlavors resolveFlavor() {
        if (fluidIngredients.isEmpty()) {
            return null;
        }

        SizedFluidIngredient ingredient = fluidIngredients.getFirst();
        for (BottledMolassesFlavors candidate : BottledMolassesFlavors.values()) {
            if (candidate.isPlain()) continue;
            FluidStack probe = new FluidStack(ModMolassesFluids.source(candidate), FLAVOR_PROBE_AMOUNT);
            if (ingredient.test(probe)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean canUseTobacco(ItemStack tobacco) {
        // Prevent the freshly produced Basin output from matching the same flavor recipe twice
        // during one mixer application.
        if (!immediateContinuationGuard.isEmpty()
                && ItemStack.isSameItemSameComponents(tobacco, immediateContinuationGuard)) {
            immediateContinuationGuard = ItemStack.EMPTY;
            return false;
        }

        return flavor != null
                && TobaccoProcessingHelper.canMechanicallyFlavorShisha(
                        tobacco, flavor.getShishaFlavorTag());
    }

    private void captureTobacco(ItemStack tobacco) {
        if (!canUseTobacco(tobacco)) {
            return;
        }

        ItemStack result = TobaccoProcessingHelper.mechanicallyFlavorShisha(
                tobacco, flavor.getShishaFlavorTag());
        if (result.isEmpty()) return;

        ItemStack template = result.copy();
        template.setCount(1);
        immediateContinuationGuard = template.copy();
        enforceNextResult(template::copy);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreateMixerCompat.TOBACCO_MIXING.get();
    }
}
