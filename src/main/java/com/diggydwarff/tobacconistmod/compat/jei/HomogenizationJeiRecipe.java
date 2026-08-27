package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Accurate JEI examples for Create's count-preserving, quality-averaging homogenizer. */
public record HomogenizationJeiRecipe(
        ItemStack lowQuality,
        ItemStack highQuality,
        ItemStack output
) {
    private static final int EXAMPLE_HALF_BATCH = 8;

    public static List<HomogenizationJeiRecipe> createAll() {
        List<HomogenizationJeiRecipe> recipes = new ArrayList<>();

        addRaw(recipes, ModItems.WILD_TOBACCO_LEAF.get());
        addRaw(recipes, ModItems.VIRGINIA_TOBACCO_LEAF.get());
        addRaw(recipes, ModItems.BURLEY_TOBACCO_LEAF.get());
        addRaw(recipes, ModItems.ORIENTAL_TOBACCO_LEAF.get());
        addRaw(recipes, ModItems.DOKHA_TOBACCO_LEAF.get());
        addRaw(recipes, ModItems.SHADE_TOBACCO_LEAF.get());

        addCured(recipes, ModItems.WILD_TOBACCO_LEAF_DRY.get());
        addCured(recipes, ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        addCured(recipes, ModItems.BURLEY_TOBACCO_LEAF_DRY.get());
        addCured(recipes, ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get());
        addCured(recipes, ModItems.DOKHA_TOBACCO_LEAF_DRY.get());
        addCured(recipes, ModItems.SHADE_TOBACCO_LEAF_DRY.get());

        return List.copyOf(recipes);
    }

    private static void addRaw(List<HomogenizationJeiRecipe> recipes, Item item) {
        ItemStack low = new ItemStack(item, EXAMPLE_HALF_BATCH);
        ItemStack high = new ItemStack(item, EXAMPLE_HALF_BATCH);
        TobaccoGrowthHelper.applyGrowthQuality(low, 40);
        TobaccoGrowthHelper.applyGrowthQuality(high, 60);
        ItemStack output = TobaccoProcessingHelper.buildHomogenizedLeafBatch(low, 50, EXAMPLE_HALF_BATCH * 2);
        recipes.add(new HomogenizationJeiRecipe(low, high, output));
    }

    private static void addCured(List<HomogenizationJeiRecipe> recipes, Item item) {
        ItemStack low = new ItemStack(item, EXAMPLE_HALF_BATCH);
        ItemStack high = new ItemStack(item, EXAMPLE_HALF_BATCH);
        TobaccoCuringHelper.applyCreativeLeafDefaults(low, true);
        TobaccoCuringHelper.applyCreativeLeafDefaults(high, true);
        setQuality(low, 40);
        setQuality(high, 80);
        ItemStack output = TobaccoProcessingHelper.buildHomogenizedLeafBatch(low, 60, EXAMPLE_HALF_BATCH * 2);
        recipes.add(new HomogenizationJeiRecipe(low, high, output));
    }

    private static void setQuality(ItemStack stack, int quality) {
        LegacyItemTags.getOrCreateTag(stack).putInt(TobaccoCuringHelper.TAG_QUALITY, quality);
        LegacyItemTags.getOrCreateTag(stack).putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(quality)
        );
    }
}
