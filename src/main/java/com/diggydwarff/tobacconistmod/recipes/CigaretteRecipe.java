package com.diggydwarff.tobacconistmod.recipes;

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
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CigaretteRecipe extends CustomRecipe {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;

    public CigaretteRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems) {
        super(id, CraftingBookCategory.MISC);
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
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
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
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

        ItemStack result = new ItemStack(ModItems.CIGARETTE.get());

        CompoundTag tag = result.getOrCreateTag();
        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));

        TobaccoDataHelper.applyTobaccoMetadata(result, tobaccoStack);

        int quality = TobaccoCuringHelper.getQuality(tobaccoStack);
        tag.putInt("InputTobaccoQuality", quality);
        tag.putString("InputCutType", TobaccoCuringHelper.getCutType(tobaccoStack));
        tag.putString("InputCureType", TobaccoCuringHelper.getCureType(tobaccoStack));

        int productQuality = Math.max(1, Math.round(quality / 10.0f));
        tag.putInt("ProductQuality", productQuality);

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CIGARETTE_RECIPE_SERIALIZER.get();
    }

    public static class Type implements RecipeType<CigaretteRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_special_cigarette";
    }

    public static class Serializer implements RecipeSerializer<CigaretteRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(TobacconistMod.MODID, "crafting_special_cigarette");

        @Override
        public CigaretteRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
            return new CigaretteRecipe(id, output, inputs);
        }

        @Override
        public CigaretteRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }
            ItemStack output = buf.readItem();
            return new CigaretteRecipe(id, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CigaretteRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(null), false);
        }
    }
}