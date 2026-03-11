package com.diggydwarff.tobacconistmod.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record TobaccoBoxLabelJeiRecipe(
        ItemStack box,
        ItemStack label,
        ItemStack output
) {
    public static List<TobaccoBoxLabelJeiRecipe> createAll() {
        ItemStack stored = JeiItemLists.getAllLooseTobaccos().get(1);
        ItemStack box = JeiItemLists.makeFilledBox(stored, 8);
        ItemStack label = JeiItemLists.makeNamedLabel("Captain's Reserve");
        ItemStack output = JeiItemLists.makeLabeledBox(stored, 8, "Captain's Reserve");

        return List.of(new TobaccoBoxLabelJeiRecipe(box, label, output));
    }
}