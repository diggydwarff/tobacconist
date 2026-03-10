package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class LooseTobaccoCuttingRecipe extends CustomRecipe {

    public LooseTobaccoCuttingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !assemble(container, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        int leafSlot = -1;
        int chavetaSlot = -1;
        int nonEmpty = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            nonEmpty++;

            if (TobaccoCuringHelper.isDryTobaccoLeaf(stack)) {
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

        int width = container.getWidth();
        int leafX = leafSlot % width;
        int leafY = leafSlot / width;
        int chavetaX = chavetaSlot % width;
        int chavetaY = chavetaSlot / width;

        int dx = chavetaX - leafX;
        int dy = chavetaY - leafY;

        String cutType;
        if (dx == -1 && dy == 0) {
            cutType = TobaccoCuringHelper.CUT_RIBBON;
        } else if (dx == 1 && dy == 0) {
            cutType = TobaccoCuringHelper.CUT_ROUGH;
        } else if (dx == 0 && dy == -1) {
            cutType = TobaccoCuringHelper.CUT_SHAG;
        } else if (dx == 0 && dy == 1) {
            cutType = TobaccoCuringHelper.CUT_FLAKE;
        } else {
            return ItemStack.EMPTY;
        }

        ItemStack leaf = container.getItem(leafSlot);
        ItemStack result = TobaccoCuringHelper.getLooseTobaccoForDryLeaf(leaf, 3);
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }

        TobaccoCuringHelper.copyTobaccoProcessingData(leaf, result);
        TobaccoCuringHelper.setCutType(result, cutType);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

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