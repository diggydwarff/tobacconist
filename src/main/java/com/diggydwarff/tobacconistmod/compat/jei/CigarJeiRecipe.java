package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record CigarJeiRecipe(
        ItemStack tobacco,
        ItemStack wrapperLeaf,
        ItemStack output
) {
    public static List<CigarJeiRecipe> createAll() {
        List<CigarJeiRecipe> recipes = new ArrayList<>();

        add(recipes, ModItems.TOBACCO_LOOSE_WILD.get(), ModItems.WILD_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.TOBACCO_LOOSE_VIRGINIA.get(), ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.TOBACCO_LOOSE_BURLEY.get(), ModItems.BURLEY_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.TOBACCO_LOOSE_ORIENTAL.get(), ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.TOBACCO_LOOSE_DOKHA.get(), ModItems.DOKHA_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.TOBACCO_LOOSE_SHADE.get(), ModItems.SHADE_TOBACCO_LEAF_DRY.get());
        add(recipes, ModItems.BLENDED_TOBACCO.get(), ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get());

        return recipes;
    }

    private static void add(List<CigarJeiRecipe> recipes, Item looseItem, Item leafItem) {
        for (ItemStack tobacco : JeiItemLists.getAllCuts(looseItem)) {
            LegacyItemTags.getOrCreateTag(tobacco).putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
            LegacyItemTags.getOrCreateTag(tobacco).putString(
                    TobaccoCuringHelper.TAG_QUALITY_TIER,
                    TobaccoCuringHelper.getQualityTierId(60)
            );
            LegacyItemTags.getOrCreateTag(tobacco).putString(TobaccoCuringHelper.TAG_CURE_TYPE, TobaccoCuringHelper.CURE_AIR);
            ItemStack wrapperLeaf = new ItemStack(leafItem);
            TobaccoCuringHelper.applyCreativeLeafDefaults(wrapperLeaf, true);
            ItemStack output = com.diggydwarff.tobacconistmod.util.TobaccoProductCraftingHelper.makeCigar(tobacco, wrapperLeaf);
            recipes.add(new CigarJeiRecipe(tobacco, wrapperLeaf, output));
        }
    }
}