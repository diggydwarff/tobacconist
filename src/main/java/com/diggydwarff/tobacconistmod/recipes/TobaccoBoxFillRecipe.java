package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TobaccoBoxFillRecipe extends CustomRecipe {

    public TobaccoBoxFillRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        return !pack(inv).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
        return pack(inv);
    }

    private ItemStack pack(CraftingContainer inv) {
        ItemStack box = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(ModItems.TOBACCO_BOX.get())) continue;
            if (!box.isEmpty()) return ItemStack.EMPTY;
            box = stack.copyWithCount(1);
        }
        if (box.isEmpty()) return ItemStack.EMPTY;

        int incomingCount = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || stack.is(ModItems.TOBACCO_BOX.get())) continue;
            // Crafting consumes one item per occupied slot, irrespective of stack size.
            // Pack only into the copy so recipe checks never change any input or its label.
            if (TobaccoBoxHelper.insert(box, stack, 1) != 1) return ItemStack.EMPTY;
            incomingCount++;
        }
        return incomingCount == 0 ? ItemStack.EMPTY : box;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TOBACCO_BOX_FILL_RECIPE_SERIALIZER.get();
    }


}