package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

/**
 * Create Deployer integration for staged Chaveta cutting.
 *
 * <p>The recipe is generated at deployer recipe-search time from the actual input stack. That is
 * intentional: Create processing recipes normally have static outputs, while Tobacconist tobacco
 * carries per-stack processing data that must survive the operation.</p>
 */
public final class CreateDeployerCompat {
    /**
     * Higher than Create's ordinary deployer/item-application recipes (50), but lower than
     * sequenced-assembly deploying (100). This lets Tobacconist own Chaveta cutting without
     * interfering with a sequenced assembly step.
     */
    private static final int CHAVETA_RECIPE_PRIORITY = 75;

    private CreateDeployerCompat() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CreateDeployerCompat::onDeployerRecipeSearch);
        TobacconistMod.LOGGER.info("Create Deployer + Chaveta cutting integration enabled.");
    }

    private static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        if (!event.shouldAddRecipeWithPriority(CHAVETA_RECIPE_PRIORITY)) {
            return;
        }

        ItemStack input = event.getInventory().getItem(0);
        ItemStack heldItem = event.getInventory().getItem(1);

        if (input.isEmpty() || heldItem.isEmpty() || !TobaccoCuringHelper.isChaveta(heldItem)) {
            return;
        }

        String nextCut = TobaccoProcessingHelper.getNextMechanicalCut(input);
        if (nextCut.isEmpty()) {
            return;
        }

        ItemStack result = TobaccoProcessingHelper.mechanicallyCutOne(input);
        if (result.isEmpty()) {
            return;
        }

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                TobacconistMod.MODID,
                "create/chaveta_cutting_" + nextCut
        );

        DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(
                DeployerApplicationRecipe::new,
                recipeId
        )
                .require(input.getItem())
                .require(heldItem.getItem())
                .output(result)
                .build();

        RecipeHolder<DeployerApplicationRecipe> holder = new RecipeHolder<>(recipeId, recipe);
        event.addRecipe(() -> Optional.of(holder), CHAVETA_RECIPE_PRIORITY);
    }
}
