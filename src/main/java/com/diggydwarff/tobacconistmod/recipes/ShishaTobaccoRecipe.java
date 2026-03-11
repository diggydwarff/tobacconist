package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobaccoItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
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

public class ShishaTobaccoRecipe extends CustomRecipe {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;

    public ShishaTobaccoRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems) {
        super(id, CraftingBookCategory.MISC);
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        int flavorCount = 0;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
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
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack tobaccoStack = ItemStack.EMPTY;
        ItemStack flavorStack1 = ItemStack.EMPTY;
        ItemStack flavorStack2 = ItemStack.EMPTY;
        ItemStack flavorStack3 = ItemStack.EMPTY;

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
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
        CompoundTag tag = new CompoundTag();

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));
        tag.putString("flavor1", flavorStack1.getDisplayName().getString());
        tag.putString("flavor2", flavorStack2.isEmpty() ? "" : flavorStack2.getDisplayName().getString());
        tag.putString("flavor3", flavorStack3.isEmpty() ? "" : flavorStack3.getDisplayName().getString());

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
        // copy aging / fermentation
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

        returnStack.setTag(tag);
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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
        NonNullList<ItemStack> remains = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
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

    public static class Type implements RecipeType<ShishaTobaccoRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_special_shishatobacco";
    }

    public static class Serializer implements RecipeSerializer<ShishaTobaccoRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(TobacconistMod.MODID, "crafting_special_shishatobacco");

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
    }
}