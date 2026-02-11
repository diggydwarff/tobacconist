package com.diggydwarff.tobacconistmod.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class ShishaTobaccoRecipe extends CustomRecipe {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;

    public ShishaTobaccoRecipe(ResourceLocation id, ItemStack output,
                               NonNullList<Ingredient> recipeItems) {
        super(id, CraftingBookCategory.MISC);
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;

    }

    public boolean matches(CraftingContainer craftingContainer, Level level) {

        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack flavorStack1 = ItemStack.EMPTY;
        ItemStack flavorStack2 = ItemStack.EMPTY;
        ItemStack flavorStack3 = ItemStack.EMPTY;

        for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {

            itemstack = craftingContainer.getItem(i);

            if (!itemstack.isEmpty()) {

                if (itemstack.getItem() instanceof LooseTobaccoItem) {
                    tobaccoStack = itemstack;
                }
                else if (itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack1.isEmpty()) {
                    flavorStack1 = itemstack;
                }
                else if(itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack2.isEmpty()){
                    flavorStack2 = itemstack;
                }
                else if(itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack3.isEmpty()){
                    flavorStack3 = itemstack;
                } else {
                    //return false;
                }
            }
        }

        return !tobaccoStack.isEmpty() && !flavorStack1.isEmpty();

    }

    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess pRegistryAccess) {

        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack flavorStack1 = ItemStack.EMPTY;
        ItemStack flavorStack2 = ItemStack.EMPTY;
        ItemStack flavorStack3 = ItemStack.EMPTY;


        for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {

            itemstack = craftingContainer.getItem(i);

            if (!itemstack.isEmpty()) {

                if (itemstack.getItem() instanceof LooseTobaccoItem) {
                    tobaccoStack = itemstack;
                }
                else if (itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack1.isEmpty()) {
                    flavorStack1 = itemstack;
                }
                else if(itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack2.isEmpty()){
                    flavorStack2 = itemstack;
                }
                else if(itemstack.getItem() instanceof ShishaFlavoringItem && flavorStack3.isEmpty()){
                    flavorStack3 = itemstack;
                }
            }
        }

        Item newItem = ModItems.SHISHA_TOBACCO.get();

        ItemStack returnStack = new ItemStack(newItem, 1);
        CompoundTag compoundtag = new CompoundTag();

        compoundtag.putString("tobacco", tobaccoStack.getDisplayName().getString());
        compoundtag.putString("flavor1", flavorStack1.getDisplayName().getString());
        compoundtag.putString("flavor2", flavorStack2.getDisplayName().getString());
        compoundtag.putString("flavor3", flavorStack3.getDisplayName().getString());

        returnStack.setTag(compoundtag);

        return returnStack;

    }

    public boolean canCraftInDimensions(int p_44298_, int p_44299_) {
        return p_44298_ * p_44299_ >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHISHA_TOBACCO_RECIPE_SERIALIZER.get();
    }

    public static class Type implements RecipeType<ShishaTobaccoRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_special_shishatobacco";
    }

    public static class Serializer implements RecipeSerializer<ShishaTobaccoRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(TobacconistMod.MODID,"crafting_special_shishatobacco");

        @Override
        public ShishaTobaccoRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new ShishaTobaccoRecipe(id, output, inputs);
        }

        @Override
        public ShishaTobaccoRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            return new ShishaTobaccoRecipe(id, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShishaTobaccoRecipe recipe) {
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