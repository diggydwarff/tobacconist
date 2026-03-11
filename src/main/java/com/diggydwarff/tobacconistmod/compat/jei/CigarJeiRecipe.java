package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import net.minecraft.nbt.CompoundTag;
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

        return recipes;
    }

    private static void add(List<CigarJeiRecipe> recipes, Item looseItem, Item leafItem) {
        ItemStack tobacco = new ItemStack(looseItem);
        tobacco.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        tobacco.getOrCreateTag().putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(60));
        TobaccoCuringHelper.setCutType(tobacco, TobaccoCuringHelper.CUT_RIBBON);
        tobacco.getOrCreateTag().putString(TobaccoCuringHelper.TAG_CURE_TYPE, TobaccoCuringHelper.CURE_AIR);

        ItemStack wrapperLeaf = new ItemStack(leafItem);
        TobaccoCuringHelper.applyCreativeLeafDefaults(wrapperLeaf, true);

        ItemStack output = new ItemStack(ModItems.CIGAR.get());
        CompoundTag tag = new CompoundTag();

        CompoundTag wrapperData = wrapperLeaf.getTag();
        if (wrapperData != null) {
            tag.put("WrapperLeafData", wrapperData.copy());
        }

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobacco));
        tag.putString("wrapper", wrapperLeaf.getDisplayName().getString());

        String cutType = TobaccoCuringHelper.getCutType(tobacco);
        if (!cutType.isEmpty()) {
            tag.putString(TobaccoCuringHelper.TAG_CUT_TYPE, cutType);
        }

        String cureType = TobaccoCuringHelper.getCureType(tobacco);
        if (!cureType.isEmpty()) {
            tag.putString(TobaccoCuringHelper.TAG_CURE_TYPE, cureType);
        }

        int quality = TobaccoCuringHelper.getQuality(tobacco);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, quality);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(quality));

        CompoundTag tobaccoData = tobacco.getTag();
        if (tobaccoData != null) {
            tag.put("PackedTobaccoData", tobaccoData.copy());
        }

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag,
                tobacco,
                TobaccoProductQualityHelper.getCigarQuality(tobacco)
        );

        output.setTag(tag);

        recipes.add(new CigarJeiRecipe(tobacco, wrapperLeaf, output));
    }
}