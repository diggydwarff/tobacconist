package com.diggydwarff.tobacconistmod.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record LabelDuplicateJeiRecipe(
        ItemStack namedLabel,
        ItemStack blankLabel,
        ItemStack output
) {
    public static List<LabelDuplicateJeiRecipe> createAll() {
        ItemStack named = JeiItemLists.makeNamedLabel("Captain's Reserve");
        ItemStack blank = JeiItemLists.makeBlankLabel();

        ItemStack output = JeiItemLists.makeNamedLabel("Captain's Reserve");
        output.setCount(2);

        return List.of(new LabelDuplicateJeiRecipe(named, blank, output));
    }
}