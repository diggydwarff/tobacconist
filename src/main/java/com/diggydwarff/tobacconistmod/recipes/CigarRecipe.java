package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoLeafItem;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CigarRecipe extends CustomRecipe {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;

    public CigarRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems) {
        super(id, CraftingBookCategory.MISC);
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack tobaccoLeafStack = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                if (!tobaccoStack.isEmpty()) return false;
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof TobaccoLeafItem) {
                if (!tobaccoLeafStack.isEmpty()) return false;
                tobaccoLeafStack = itemstack;
            } else {
                return false;
            }
        }

        return !tobaccoStack.isEmpty() && !tobaccoLeafStack.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack tobaccoLeafStack = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemstack = craftingContainer.getItem(i);
            if (itemstack.isEmpty()) continue;

            if (itemstack.getItem() instanceof LooseTobaccoItem) {
                tobaccoStack = itemstack;
            } else if (itemstack.getItem() instanceof TobaccoLeafItem) {
                tobaccoLeafStack = itemstack;
            }
        }

        if (tobaccoStack.isEmpty() || tobaccoLeafStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item newItem = ModItems.CIGAR.get();
        ItemStack returnStack = new ItemStack(newItem, 1);
        CompoundTag tag = new CompoundTag();

        CompoundTag wrapperData = tobaccoLeafStack.getTag();
        if (wrapperData != null) {
            tag.put("WrapperLeafData", wrapperData.copy());
        }

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));
        tag.putString("wrapper", tobaccoLeafStack.getDisplayName().getString());

        String cutType = TobaccoCuringHelper.getCutType(tobaccoStack);
        if (!cutType.isEmpty()) {
            tag.putString(TobaccoCuringHelper.TAG_CUT_TYPE, cutType);
        }

        String cureType = TobaccoCuringHelper.getCureType(tobaccoStack);
        if (!cureType.isEmpty()) {
            tag.putString(TobaccoCuringHelper.TAG_CURE_TYPE, cureType);
        }

        int quality = TobaccoCuringHelper.getQuality(tobaccoStack);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, quality);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(quality));

        CompoundTag tobaccoData = tobaccoStack.getTag();
        if (tobaccoData != null) {
            tag.put("PackedTobaccoData", tobaccoData.copy());
        }
        // merge aging data from filler + wrapper
        CompoundTag fillerTag = tobaccoStack.getTag();
        CompoundTag wrapperTag = tobaccoLeafStack.getTag();

        int fillerAge = fillerTag != null ? fillerTag.getInt("AgedDays") : 0;
        int wrapperAge = wrapperTag != null ? wrapperTag.getInt("AgedDays") : 0;

        int finalAge = Math.max(fillerAge, wrapperAge);

        if (finalAge > 0) {
            tag.putInt("AgedDays", finalAge);
        }

        boolean fermented =
                (fillerTag != null && fillerTag.getBoolean("Fermented")) ||
                        (wrapperTag != null && wrapperTag.getBoolean("Fermented"));

        if (fermented) {
            tag.putBoolean("Fermented", true);
        }

        boolean ruined =
                (fillerTag != null && fillerTag.getBoolean("Ruined")) ||
                        (wrapperTag != null && wrapperTag.getBoolean("Ruined"));

        if (ruined) {
            tag.putBoolean("Ruined", true);
        }

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag,
                tobaccoStack,
                TobaccoProductQualityHelper.getCigarQuality(tobaccoStack)
        );

        returnStack.setTag(tag);
        return returnStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CIGAR_RECIPE_SERIALIZER.get();
    }

    public static class Type implements RecipeType<CigarRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_special_cigar";
    }

    public static class Serializer implements RecipeSerializer<CigarRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(TobacconistMod.MODID, "crafting_special_cigar");

        @Override
        public CigarRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
            return new CigarRecipe(id, output, inputs);
        }

        @Override
        public CigarRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }
            ItemStack output = buf.readItem();
            return new CigarRecipe(id, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CigarRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(null), false);
        }
    }
}