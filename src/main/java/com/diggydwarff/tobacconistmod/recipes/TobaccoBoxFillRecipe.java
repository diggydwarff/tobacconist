package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TobaccoBoxFillRecipe extends CustomRecipe {

    public TobaccoBoxFillRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack box = ItemStack.EMPTY;
        ItemStack contentType = ItemStack.EMPTY;
        int incomingCount = 0;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.TOBACCO_BOX.get())) {
                if (!box.isEmpty()) return false;
                box = stack;
                continue;
            }

            if (!TobaccoBoxHelper.isSupportedContent(stack)) return false;

            if (contentType.isEmpty()) {
                contentType = stack.copy();
                contentType.setCount(1);
                TobaccoBoxHelper.clearCustomProductName(contentType);
            } else {
                ItemStack compare = stack.copy();
                compare.setCount(1);
                TobaccoBoxHelper.clearCustomProductName(compare);
                if (!TobaccoBoxHelper.sameContent(contentType, compare)) return false;
            }

            incomingCount += stack.getCount();
        }

        if (box.isEmpty() || contentType.isEmpty()) return false;

        if (TobaccoBoxHelper.hasStoredItem(box) && !TobaccoBoxHelper.canAccept(box, contentType)) {
            return false;
        }

        ItemStack base = TobaccoBoxHelper.hasStoredItem(box) ? TobaccoBoxHelper.getStoredItem(box) : contentType;
        int current = TobaccoBoxHelper.getStoredCount(box);
        int cap = TobaccoBoxHelper.getCapacity(base);

        return current + incomingCount <= cap;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack box = ItemStack.EMPTY;
        ItemStack content = ItemStack.EMPTY;
        int incomingCount = 0;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.TOBACCO_BOX.get())) {
                box = stack.copy();
            } else if (content.isEmpty()) {
                content = stack.copy();
                content.setCount(1);
                TobaccoBoxHelper.clearCustomProductName(content);
                incomingCount += stack.getCount();
            } else {
                incomingCount += stack.getCount();
            }
        }

        if (box.isEmpty() || content.isEmpty()) return ItemStack.EMPTY;

        int current = TobaccoBoxHelper.getStoredCount(box);
        ItemStack base = TobaccoBoxHelper.hasStoredItem(box) ? TobaccoBoxHelper.getStoredItem(box) : content;

        TobaccoBoxHelper.setStored(box, base, current + incomingCount);
        return box;
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

    public static class Serializer implements RecipeSerializer<TobaccoBoxFillRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public TobaccoBoxFillRecipe fromJson(ResourceLocation id, com.google.gson.JsonObject json) {
            return new TobaccoBoxFillRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public TobaccoBoxFillRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new TobaccoBoxFillRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TobaccoBoxFillRecipe recipe) {
        }
    }
}