package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
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

        // uncured
        add(recipes, ModItems.WILD_TOBACCO_LEAF.get());
        add(recipes, ModItems.VIRGINIA_TOBACCO_LEAF.get());
        add(recipes, ModItems.BURLEY_TOBACCO_LEAF.get());
        add(recipes, ModItems.ORIENTAL_TOBACCO_LEAF.get());
        add(recipes, ModItems.DOKHA_TOBACCO_LEAF.get());
        add(recipes, ModItems.SHADE_TOBACCO_LEAF.get());

        // dry
        add(recipes, ModItems.WILD_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.BURLEY_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.DOKHA_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.SHADE_TOBACCO_LEAF_DRY.get());

        return recipes;
    }

    private static void add(List<AverageLeavesJeiRecipe> recipes, Item leafItem) {
        ItemStack a = new ItemStack(leafItem);
        ItemStack b = new ItemStack(leafItem);
        ItemStack out = new ItemStack(leafItem);

        TobaccoCuringHelper.applyCreativeLeafDefaults(a, true);
        TobaccoCuringHelper.applyCreativeLeafDefaults(b, true);
        TobaccoCuringHelper.applyCreativeLeafDefaults(out, true);

        a.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 40);
        a.getOrCreateTag().putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(40));

        b.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 80);
        b.getOrCreateTag().putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(80));

        out.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        out.getOrCreateTag().putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(60));

        recipes.add(new AverageLeavesJeiRecipe(a, b, out));
    }
}