package com.diggydwarff.tobacconistmod.recipes;

import net.minecraft.resources.ResourceLocation;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.datagen.items.custom.FlavoringEssenceItem;
import com.diggydwarff.tobacconistmod.fluid.EssenceBottleFluidHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** One full essence bottle flavors one full bottle of plain molasses. */
public final class FlavorMolassesRecipe extends CustomRecipe {
    public FlavorMolassesRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return findFlavor(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        BottledMolassesFlavors flavor = findFlavor(input);
        return flavor == null ? ItemStack.EMPTY : flavor.getStack();
    }

    private BottledMolassesFlavors findFlavor(CraftingContainer input) {
        boolean plainMolasses = false;
        BottledMolassesFlavors essenceFlavor = null;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            BottledMolassesFlavors molasses = BottledMolassesFlavors.fromItem(stack.getItem());
            if (molasses == BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN) {
                if (plainMolasses) return null;
                plainMolasses = true;
                continue;
            }

            if (stack.getItem() instanceof FlavoringEssenceItem) {
                if (essenceFlavor != null) return null;
                essenceFlavor = BottledMolassesFlavors.fromEssenceItem(stack.getItem());
                if (essenceFlavor == null) return null;
                continue;
            }
            return null;
        }
        return plainMolasses ? essenceFlavor : null;
    }

    @Override public boolean isSpecial() { return true; }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remains = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!(stack.getItem() instanceof FlavoringEssenceItem)) continue;
            remains.set(i, new ItemStack(Items.GLASS_BOTTLE));
        }
        return remains;
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.FLAVOR_MOLASSES_RECIPE_SERIALIZER.get(); }
}
