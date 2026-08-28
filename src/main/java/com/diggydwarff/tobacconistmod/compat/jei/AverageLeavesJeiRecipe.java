package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record AverageLeavesJeiRecipe(
        ItemStack inputA,
        ItemStack inputB,
        ItemStack output
) {
    public static List<AverageLeavesJeiRecipe> createAll() {
        List<AverageLeavesJeiRecipe> recipes = new ArrayList<>();

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

    private static void addRaw(List<AverageLeavesJeiRecipe> recipes, Item leafItem) {
        ItemStack a = new ItemStack(leafItem);
        ItemStack b = new ItemStack(leafItem);
        ItemStack out = new ItemStack(leafItem, 2);
        TobaccoGrowthHelper.applyGrowthQuality(a, 40);
        TobaccoGrowthHelper.applyGrowthQuality(b, 60);
        TobaccoGrowthHelper.applyGrowthQuality(out, 50);
        recipes.add(new AverageLeavesJeiRecipe(a, b, out));
    }

    private static void addCured(List<AverageLeavesJeiRecipe> recipes, Item leafItem) {
        ItemStack a = new ItemStack(leafItem);
        ItemStack b = new ItemStack(leafItem);
        ItemStack out = new ItemStack(leafItem, 2);

        TobaccoCuringHelper.applyCreativeLeafDefaults(a, true);
        TobaccoCuringHelper.applyCreativeLeafDefaults(b, true);
        TobaccoCuringHelper.applyCreativeLeafDefaults(out, true);

        setQuality(a, 40);
        setQuality(b, 80);
        setQuality(out, 60);
        recipes.add(new AverageLeavesJeiRecipe(a, b, out));
    }

    private static void setQuality(ItemStack stack, int quality) {
        LegacyItemTags.getOrCreateTag(stack).putInt(TobaccoCuringHelper.TAG_QUALITY, quality);
        LegacyItemTags.getOrCreateTag(stack).putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(quality)
        );
    }
}