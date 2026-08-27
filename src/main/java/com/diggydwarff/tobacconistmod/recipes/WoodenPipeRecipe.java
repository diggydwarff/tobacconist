package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class WoodenPipeRecipe extends ShapedRecipe {

    public static final String NBT_WOOD_PLANK = "WoodPlank";

    public WoodenPipeRecipe(CraftingBookCategory category) {
        super("", category, displayPattern(), new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get()));
    }

    private static ShapedRecipePattern displayPattern() {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        ingredients.set(2, Ingredient.of(ItemTags.PLANKS));
        ingredients.set(4, Ingredient.of(ItemTags.PLANKS));
        ingredients.set(5, Ingredient.of(Items.STICK));
        ingredients.set(6, Ingredient.of(Items.STICK));
        return new ShapedRecipePattern(3, 3, ingredients, Optional.empty());
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        if (inv.width() != 3 || inv.height() != 3) return false;

        int w = inv.width();

        ItemStack p1 = inv.getItem(2 + 0 * w); // (2,0)
        ItemStack p2 = inv.getItem(1 + 1 * w); // (1,1)
        ItemStack s1 = inv.getItem(2 + 1 * w); // (2,1)
        ItemStack s2 = inv.getItem(0 + 2 * w); // (0,2)

        if (!p1.is(net.minecraft.tags.ItemTags.PLANKS)) return false;
        if (!p2.is(net.minecraft.tags.ItemTags.PLANKS)) return false;
        if (!s1.is(net.minecraft.world.item.Items.STICK)) return false;
        if (!s2.is(net.minecraft.world.item.Items.STICK)) return false;

        for (int i = 0; i < inv.size(); i++) {
            if (i == (2 + 0 * w) || i == (1 + 1 * w) || i == (2 + 1 * w) || i == (0 + 2 * w)) continue;
            if (!inv.getItem(i).isEmpty()) return false;
        }

        return true;
    }


    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        ItemStack out = new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tobacconistmod", "wooden_smoking_pipe"))
        );

        // Pick the first plank stack found and store it
        ItemStack plank = ItemStack.EMPTY;
        for (int i : new int[]{2, 4}) {
            ItemStack s = inv.getItem(i);
            if (s.is(net.minecraft.tags.ItemTags.PLANKS)) { plank = s; break; }
        }

        if (!plank.isEmpty()) {
            CompoundTag tag = LegacyItemTags.getOrCreateTag(out);
            ResourceLocation plankId = BuiltInRegistries.ITEM.getKey(plank.getItem());
            tag.putString(NBT_WOOD_PLANK, plankId.toString());
        }

        return out;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w >= 3 && h >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WOODEN_PIPE.get();
    }


}
