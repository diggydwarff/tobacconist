package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LabelItem;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TobaccoBoxLabelRecipe extends CustomRecipe {

    public TobaccoBoxLabelRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack box = ItemStack.EMPTY;
        ItemStack label = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.TOBACCO_BOX.get())) {
                if (!box.isEmpty()) return false;
                box = stack;
            } else if (stack.is(ModItems.TOBACCO_LABEL.get())) {
                if (!label.isEmpty()) return false;
                label = stack;
            } else {
                return false;
            }
        }

        return !box.isEmpty() && !label.isEmpty() && !LabelItem.getLabelName(label).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        ItemStack box = ItemStack.EMPTY;
        ItemStack label = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.TOBACCO_BOX.get())) box = stack.copy();
            else if (stack.is(ModItems.TOBACCO_LABEL.get())) label = stack;
        }

        if (box.isEmpty() || label.isEmpty()) return ItemStack.EMPTY;

        TobaccoBoxHelper.setLabel(box, LabelItem.getLabelName(label));
        return box;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TOBACCO_BOX_LABEL_RECIPE_SERIALIZER.get();
    }


}