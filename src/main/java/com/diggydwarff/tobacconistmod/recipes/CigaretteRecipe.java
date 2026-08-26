package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.RollingPaperItem;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoDataHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CigaretteRecipe extends CustomRecipe {
    public CigaretteRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack paperStack = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                if (!tobaccoStack.isEmpty()) return false;
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof RollingPaperItem) {
                if (!paperStack.isEmpty()) return false;
                paperStack = itemstack;
            } else {
                return false;
            }
        }

        return !tobaccoStack.isEmpty() && !paperStack.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
        ItemStack paperStack = ItemStack.EMPTY;
        ItemStack tobaccoStack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.ROLLING_PAPER.get())) {
                paperStack = stack;
            } else if (TobaccoCuringHelper.isLooseTobacco(stack)) {
                tobaccoStack = stack;
            }
        }

        if (paperStack.isEmpty() || tobaccoStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return com.diggydwarff.tobacconistmod.util.TobaccoProductCraftingHelper.makeCigarette(tobaccoStack);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CIGARETTE_RECIPE_SERIALIZER.get();
    }




}