package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoDataHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ShishaTobaccoRecipe extends CustomRecipe {
    public ShishaTobaccoRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        int flavorCount = 0;

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                if (!tobaccoStack.isEmpty()) return false;
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem) {
                flavorCount++;
                if (flavorCount > 3) return false;
            } else {
                return false;
            }
        }

        return !tobaccoStack.isEmpty() && flavorCount >= 1;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider registries) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack flavorStack1 = ItemStack.EMPTY;
        ItemStack flavorStack2 = ItemStack.EMPTY;
        ItemStack flavorStack3 = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack1.isEmpty()) {
                flavorStack1 = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack2.isEmpty()) {
                flavorStack2 = itemstack;
            } else if (itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack3.isEmpty()) {
                flavorStack3 = itemstack;
            }
        }

        if (tobaccoStack.isEmpty() || flavorStack1.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item newItem = ModItems.SHISHA_TOBACCO.get();
        ItemStack returnStack = new ItemStack(newItem, 1);
        CompoundTag tag = LegacyItemTags.getOrCreateTag(returnStack);

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));
        tag.putString("flavor1", flavorStack1.getDisplayName().getString());
        tag.putString("flavor2", flavorStack2.isEmpty() ? "" : flavorStack2.getDisplayName().getString());
        tag.putString("flavor3", flavorStack3.isEmpty() ? "" : flavorStack3.getDisplayName().getString());

        TobaccoDataHelper.applyTobaccoMetadata(returnStack, tobaccoStack);

        CompoundTag tobaccoData = LegacyItemTags.getTag(tobaccoStack);
        if (tobaccoData != null) {
            if (tobaccoData.contains("AgedDays")) {
                tag.putInt("AgedDays", tobaccoData.getInt("AgedDays"));
            }

            if (tobaccoData.getBoolean("Fermented")) {
                tag.putBoolean("Fermented", true);
            }

            if (tobaccoData.getBoolean("Ruined")) {
                tag.putBoolean("Ruined", true);
            }
        }

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag,
                tobaccoStack,
                TobaccoProductQualityHelper.getShishaQuality(tobaccoStack)
        );

        return returnStack;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingContainer) {
        NonNullList<ItemStack> remains = NonNullList.withSize(craftingContainer.size(), ItemStack.EMPTY);

        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof ShishaFlavoringItem) {
                ItemStack bottle = stack.copy();
                bottle.setCount(1);

                if (bottle.isDamageableItem()) {
                    bottle.setDamageValue(bottle.getDamageValue() + 1);

                    if (bottle.getDamageValue() < bottle.getMaxDamage()) {
                        remains.set(i, bottle);
                    } else {
                        remains.set(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        return remains;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHISHA_TOBACCO_RECIPE_SERIALIZER.get();
    }




}