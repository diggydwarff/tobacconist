package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BrassNameTagItem;
import com.diggydwarff.tobacconistmod.util.TobaccoAssemblyHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoLabelHelper;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/** Dynamic Create Deployer recipes that preserve Tobacconist per-stack metadata. */
public final class CreateDeployerCompat {
    private static final int BRASS_LABEL_PRIORITY = 95;
    private static final int BOX_PACKING_PRIORITY = 90;
    private static final int PRODUCT_ASSEMBLY_PRIORITY = 85;
    private static final int CHAVETA_RECIPE_PRIORITY = 75;

    // Dynamic packing recipes are generated from the actual Deployer-held stack. Create's
    // normal ItemApplicationRecipe consumption is always exactly one item, so bulk box packing
    // marks the held stack as a reusable tool and the belt callback consumes this recorded count.
    private static final Map<Recipe<?>, Integer> BULK_PACKING_COUNTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CreateDeployerCompat() {}

    public static int getBulkPackingConsumption(Recipe<?> recipe) {
        if (recipe == null) return 0;
        return BULK_PACKING_COUNTS.getOrDefault(recipe, 0);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CreateDeployerCompat::onDeployerRecipeSearch);
        TobacconistMod.LOGGER.info("Create Deployer cutting + product assembly + reusable box labeling/packing integration enabled.");
    }

    private static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        ItemStack input = event.getInventory().getItem(0);
        ItemStack heldItem = event.getInventory().getItem(1);
        if (input.isEmpty() || heldItem.isEmpty()) return;

        if (tryBrassBoxLabel(event, input, heldItem)) return;
        if (tryBoxPacking(event, input, heldItem)) return;
        if (tryProductAssembly(event, input, heldItem)) return;
        tryChavetaCutting(event, input, heldItem);
    }

    private static boolean tryBrassBoxLabel(DeployerRecipeSearchEvent event, ItemStack input, ItemStack heldItem) {
        if (!event.shouldAddRecipeWithPriority(BRASS_LABEL_PRIORITY)
                || !input.is(ModItems.TOBACCO_BOX.get())
                || !heldItem.is(ModItems.BRASS_NAME_TAG.get())) {
            return false;
        }

        String stampName = BrassNameTagItem.getStampName(heldItem);
        if (stampName.isEmpty()) return false;

        ItemStack result = input.copy();
        result.setCount(1);
        TobaccoBoxHelper.setLabel(result, stampName);

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                TobacconistMod.MODID,
                "create/brass_name_tag_box_label"
        );

        DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(
                DeployerApplicationRecipe::new,
                recipeId
        )
                .require(input.getItem())
                .require(heldItem.getItem())
                .toolNotConsumed()
                .output(result)
                .build();

        event.addRecipe(() -> Optional.of(new RecipeHolder<>(recipeId, recipe)), BRASS_LABEL_PRIORITY);
        return true;
    }

    private static boolean tryBoxPacking(DeployerRecipeSearchEvent event, ItemStack input, ItemStack heldItem) {
        if (!event.shouldAddRecipeWithPriority(BOX_PACKING_PRIORITY)
                || !input.is(ModItems.TOBACCO_BOX.get())
                || !TobaccoBoxHelper.isSupportedContent(heldItem)) {
            return false;
        }

        BoxPackingResult packed = packIntoBox(input, heldItem);
        if (packed.box().isEmpty() || packed.consumed() <= 0) return false;

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                TobacconistMod.MODID,
                "create/tobacco_box_pack"
        );

        DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(
                DeployerApplicationRecipe::new,
                recipeId
        )
                .require(input.getItem())
                .require(heldItem.getItem())
                .toolNotConsumed()
                .output(packed.box())
                .build();

        BULK_PACKING_COUNTS.put(recipe, packed.consumed());
        event.addRecipe(() -> Optional.of(new RecipeHolder<>(recipeId, recipe)), BOX_PACKING_PRIORITY);
        return true;
    }

    private static BoxPackingResult packIntoBox(ItemStack boxInput, ItemStack incoming) {
        ItemStack box = boxInput.copy();
        box.setCount(1);

        ItemStack normalized = incoming.copy();
        normalized.setCount(1);
        String incomingLabel = TobaccoLabelHelper.getProductLabel(normalized);
        TobaccoLabelHelper.clearProductLabel(normalized);
        TobaccoBoxHelper.clearCustomProductName(normalized);

        ItemStack stored = TobaccoBoxHelper.getStoredItem(box);
        int count = TobaccoBoxHelper.getStoredCount(box);

        if (!stored.isEmpty() && !TobaccoBoxHelper.sameContent(stored, normalized)) {
            return BoxPackingResult.EMPTY;
        }

        ItemStack capacityStack = stored.isEmpty() ? normalized : stored;
        int remaining = TobaccoBoxHelper.getCapacity(capacityStack) - count;
        if (remaining <= 0) return BoxPackingResult.EMPTY;

        String boxLabel = TobaccoBoxHelper.getLabel(box);
        if (!boxLabel.isEmpty() && !incomingLabel.isEmpty() && !boxLabel.equals(incomingLabel)) {
            return BoxPackingResult.EMPTY;
        }
        if (boxLabel.isEmpty() && !incomingLabel.isEmpty()) {
            TobaccoBoxHelper.setLabel(box, incomingLabel);
        }

        int transfer = Math.min(remaining, incoming.getCount());
        if (transfer <= 0) return BoxPackingResult.EMPTY;

        TobaccoBoxHelper.setStored(box, capacityStack, count + transfer);
        return new BoxPackingResult(box, transfer);
    }

    private record BoxPackingResult(ItemStack box, int consumed) {
        private static final BoxPackingResult EMPTY = new BoxPackingResult(ItemStack.EMPTY, 0);
    }

    private static boolean tryProductAssembly(DeployerRecipeSearchEvent event, ItemStack input, ItemStack heldItem) {
        if (!event.shouldAddRecipeWithPriority(PRODUCT_ASSEMBLY_PRIORITY)
                || !TobaccoCuringHelper.isLooseTobacco(input)) {
            return false;
        }

        ItemStack incomplete;
        String recipePath;

        if (heldItem.is(ModItems.ROLLING_PAPER.get())) {
            incomplete = TobaccoAssemblyHelper.makeIncompleteCigarette(input);
            recipePath = "create/cigarette_assembly_deploy";
        } else if (TobaccoCuringHelper.isDryTobaccoLeaf(heldItem)) {
            incomplete = TobaccoAssemblyHelper.makeIncompleteCigar(input, heldItem);
            recipePath = "create/cigar_assembly_deploy";
        } else {
            return false;
        }

        if (incomplete.isEmpty()) return false;

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, recipePath);
        DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(
                DeployerApplicationRecipe::new,
                recipeId
        )
                .require(input.getItem())
                .require(heldItem.getItem())
                .output(incomplete)
                .build();

        event.addRecipe(() -> Optional.of(new RecipeHolder<>(recipeId, recipe)), PRODUCT_ASSEMBLY_PRIORITY);
        return true;
    }

    private static boolean tryChavetaCutting(DeployerRecipeSearchEvent event, ItemStack input, ItemStack heldItem) {
        if (!event.shouldAddRecipeWithPriority(CHAVETA_RECIPE_PRIORITY)
                || !TobaccoCuringHelper.isChaveta(heldItem)) {
            return false;
        }

        String nextCut = TobaccoProcessingHelper.getNextMechanicalCut(input);
        if (nextCut.isEmpty()) return false;

        ItemStack result = TobaccoProcessingHelper.mechanicallyCutOne(input);
        if (result.isEmpty()) return false;

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
                .toolNotConsumed()
                .output(result)
                .build();

        event.addRecipe(() -> Optional.of(new RecipeHolder<>(recipeId, recipe)), CHAVETA_RECIPE_PRIORITY);
        return true;
    }
}
