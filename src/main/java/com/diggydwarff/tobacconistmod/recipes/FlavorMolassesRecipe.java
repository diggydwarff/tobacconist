package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.compat.FlavorCompatibility;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.datagen.items.custom.FlavoringEssenceItem;
import com.diggydwarff.tobacconistmod.fluid.EssenceBottleFluidHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** One full essence bottle flavors one full bottle of plain molasses. */
public final class FlavorMolassesRecipe extends CustomRecipe {
    public FlavorMolassesRecipe(CraftingBookCategory category) { super(category); }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findFlavor(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        BottledMolassesFlavors flavor = findFlavor(input);
        return flavor == null ? ItemStack.EMPTY : flavor.getStack();
    }

    private BottledMolassesFlavors findFlavor(CraftingInput input) {
        boolean plainMolasses = false;
        BottledMolassesFlavors essenceFlavor = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (FlavorCompatibility.isPlainMolasses(stack)) {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remains = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof FlavoringEssenceItem) {
                remains.set(i, new ItemStack(Items.GLASS_BOTTLE));
            } else if (FlavorCompatibility.isPlainMolasses(stack) && stack.hasCraftingRemainingItem()) {
                remains.set(i, stack.getCraftingRemainingItem());
            }
        }
        return remains;
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.FLAVOR_MOLASSES_RECIPE_SERIALIZER.get(); }
}
