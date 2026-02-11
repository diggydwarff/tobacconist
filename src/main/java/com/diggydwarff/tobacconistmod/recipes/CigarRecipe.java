package com.diggydwarff.tobacconistmod.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoLeafItem;
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

    public CigarRecipe(ResourceLocation id, ItemStack output,
                       NonNullList<Ingredient> recipeItems) {
        super(id, CraftingBookCategory.MISC);
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;

    }

    public boolean matches(CraftingContainer craftingContainer, Level level) {

        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack tobaccoLeafStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {

            itemstack = craftingContainer.getItem(i);

            if (!itemstack.isEmpty()) {

                if (itemstack.getItem() instanceof LooseTobaccoItem) {
                    tobaccoStack = itemstack;
                } else if (itemstack.getItem() instanceof TobaccoLeafItem) {
                    tobaccoLeafStack = itemstack;
                } else {
                    return false;
                }
            }
        }

        return !tobaccoStack.isEmpty() && !tobaccoLeafStack.isEmpty();

    }

    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess pRegistryAccess) {

        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack tobaccoLeafStack = ItemStack.EMPTY;

        for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {

            itemstack = craftingContainer.getItem(i);

            if (!itemstack.isEmpty()) {

                if (itemstack.getItem() instanceof LooseTobaccoItem) {
                    tobaccoStack = itemstack;
                } else if (itemstack.getItem() instanceof TobaccoLeafItem) {
                    tobaccoLeafStack = itemstack;
                }
            }
        }

        Item newItem = ModItems.CIGAR.get();

        ItemStack returnStack = new ItemStack(newItem, 1);
        CompoundTag compoundtag = new CompoundTag();

        compoundtag.putString("tobacco", tobaccoStack.getDisplayName().getString());
        compoundtag.putString("wrapper", tobaccoLeafStack.getDisplayName().getString());

        returnStack.setTag(compoundtag);

        return returnStack;

    }

    public boolean canCraftInDimensions(int p_44298_, int p_44299_) {
        return p_44298_ * p_44299_ >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CIGAR_RECIPE_SERIALIZER.get();
    }

    public static class Type implements RecipeType<CigarRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_special_cigar";
    }

    public static class Serializer implements RecipeSerializer<CigarRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(TobacconistMod.MODID,"crafting_special_cigar");

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

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}