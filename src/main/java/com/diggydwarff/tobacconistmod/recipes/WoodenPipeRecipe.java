package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

public class WoodenPipeRecipe extends CustomRecipe {

    public static final String NBT_WOOD_PLANK = "WoodPlank";

    public WoodenPipeRecipe(ResourceLocation id, CraftingBookCategory cat) {
        super(id, cat);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (inv.getWidth() != 3 || inv.getHeight() != 3) return false;

        int w = inv.getWidth();

        ItemStack p1 = inv.getItem(2 + 0 * w); // (2,0)
        ItemStack p2 = inv.getItem(1 + 1 * w); // (1,1)
        ItemStack s1 = inv.getItem(2 + 1 * w); // (2,1)
        ItemStack s2 = inv.getItem(0 + 2 * w); // (0,2)

        if (!p1.is(net.minecraft.tags.ItemTags.PLANKS)) return false;
        if (!p2.is(net.minecraft.tags.ItemTags.PLANKS)) return false;
        if (!s1.is(net.minecraft.world.item.Items.STICK)) return false;
        if (!s2.is(net.minecraft.world.item.Items.STICK)) return false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (i == (2 + 0 * w) || i == (1 + 1 * w) || i == (2 + 1 * w) || i == (0 + 2 * w)) continue;
            if (!inv.getItem(i).isEmpty()) return false;
        }

        return true;
    }


    @Override
    public ItemStack assemble(CraftingContainer inv, net.minecraft.core.RegistryAccess regs) {
        ItemStack out = new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get());

        // Pick the first plank stack found and store it
        ItemStack plank = ItemStack.EMPTY;
        for (int i : new int[]{2, 4}) {
            ItemStack s = inv.getItem(i);
            if (s.is(net.minecraft.tags.ItemTags.PLANKS)) { plank = s; break; }
        }

        if (!plank.isEmpty()) {
            CompoundTag tag = out.getOrCreateTag();
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

    public static class Serializer implements RecipeSerializer<WoodenPipeRecipe> {
        @Override
        public WoodenPipeRecipe fromJson(ResourceLocation id, com.google.gson.JsonObject json) {
            return new WoodenPipeRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public WoodenPipeRecipe fromNetwork(ResourceLocation id, net.minecraft.network.FriendlyByteBuf buf) {
            return new WoodenPipeRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf buf, WoodenPipeRecipe recipe) {
            // no extra data
        }
    }
}
