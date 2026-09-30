package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class LooseTobaccoCuttingRecipe extends CustomRecipe {

    public LooseTobaccoCuttingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return !assemble(container, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        int leafSlot = -1;
        int chavetaSlot = -1;
        int nonEmpty = 0;

        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            nonEmpty++;

            if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)
                    || (TobaccoCuringHelper.isLooseTobacco(stack)
                    && TobaccoCuringHelper.CUT_PLUG.equals(TobaccoCuringHelper.getCutType(stack)))) {
                if (leafSlot != -1) return ItemStack.EMPTY;
                leafSlot = i;
            } else if (TobaccoCuringHelper.isChaveta(stack)) {
                if (chavetaSlot != -1) return ItemStack.EMPTY;
                chavetaSlot = i;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (nonEmpty != 2 || leafSlot == -1 || chavetaSlot == -1) {
            return ItemStack.EMPTY;
        }

        int width = container.width();
        int leafX = leafSlot % width;
        int leafY = leafSlot / width;
        int chavetaX = chavetaSlot % width;
        int chavetaY = chavetaSlot / width;

        int dx = chavetaX - leafX;
        int dy = chavetaY - leafY;

        ItemStack leaf = container.getItem(leafSlot);
        if (TobaccoCuringHelper.isLooseTobacco(leaf)) {
            // A Plug has one intentional hand-cut route: slice it into Flake. It must not be
            // reversible back into Rough/Ribbon/Shag merely by moving the Chaveta around.
            if (dx != 0 || dy != 1
                    || !TobaccoCuringHelper.CUT_PLUG.equals(TobaccoCuringHelper.getCutType(leaf))) {
                return ItemStack.EMPTY;
            }
            return TobaccoProcessingHelper.recutLooseTobacco(leaf, TobaccoCuringHelper.CUT_FLAKE);
        }

        String cutType;
        if (dx == -1 && dy == 0) {
            cutType = TobaccoCuringHelper.CUT_RIBBON;
        } else if (dx == 1 && dy == 0) {
            cutType = TobaccoCuringHelper.CUT_ROUGH;
        } else if (dx == 0 && dy == -1) {
            cutType = TobaccoCuringHelper.CUT_SHAG;
        } else {
            return ItemStack.EMPTY;
        }

        return TobaccoProcessingHelper.cutDryLeaf(leaf, cutType, 3);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && TobaccoCuringHelper.isChaveta(stack)) {
                remaining.set(i, stack.getCraftingRemainingItem());
            }
        }

        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LOOSE_TOBACCO_CUTTING_SERIALIZER.get();
    }
}