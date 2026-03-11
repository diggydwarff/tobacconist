package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record TobaccoBoxFillJeiRecipe(
        ItemStack emptyBox,
        List<ItemStack> contents,
        List<ItemStack> outputs
) {
    public static List<TobaccoBoxFillJeiRecipe> createAll() {
        List<ItemStack> contents = JeiItemLists.getAllTobaccoBoxSupportedContents();
        List<ItemStack> outputs = new ArrayList<>();

        for (ItemStack content : contents) {
            int count = Math.min(TobaccoBoxHelper.getCapacity(content), 4);
            outputs.add(JeiItemLists.makeFilledBox(content, count));
        }

        return List.of(new TobaccoBoxFillJeiRecipe(
                new ItemStack(com.diggydwarff.tobacconistmod.datagen.items.ModItems.TOBACCO_BOX.get()),
                contents,
                outputs
        ));
    }
}