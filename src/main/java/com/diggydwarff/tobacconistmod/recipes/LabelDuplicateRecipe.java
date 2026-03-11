package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LabelItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class LabelDuplicateRecipe extends CustomRecipe {

    public LabelDuplicateRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        int blank = 0;
        ItemStack named = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (!stack.is(ModItems.TOBACCO_LABEL.get())) return false;

            String label = LabelItem.getLabelName(stack);
            if (label.isEmpty()) blank++;
            else {
                if (!named.isEmpty()) return false;
                named = stack;
            }
        }

        return !named.isEmpty() && blank == 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack named = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            String label = LabelItem.getLabelName(stack);
            if (!label.isEmpty()) {
                named = stack;
                break;
            }
        }

        if (named.isEmpty()) return ItemStack.EMPTY;

        ItemStack out = new ItemStack(ModItems.TOBACCO_LABEL.get(), 2);
        String copiedName = LabelItem.getLabelName(named);
        LabelItem.setLabelName(out, copiedName);
        out.setHoverName(named.getHoverName());
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LABEL_DUPLICATE_RECIPE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<LabelDuplicateRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public LabelDuplicateRecipe fromJson(ResourceLocation id, com.google.gson.JsonObject json) {
            return new LabelDuplicateRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public LabelDuplicateRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new LabelDuplicateRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LabelDuplicateRecipe recipe) {
        }
    }
}