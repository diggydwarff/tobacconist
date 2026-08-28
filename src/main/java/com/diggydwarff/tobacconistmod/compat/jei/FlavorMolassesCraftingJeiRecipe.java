package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.compat.FlavorCompatibility;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

/** JEI-only vanilla crafting-grid examples for the dynamic plain-molasses + essence recipe. */
public final class FlavorMolassesCraftingJeiRecipe {
    private FlavorMolassesCraftingJeiRecipe() {}

    public static List<CraftingRecipe> createAll() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.isPlain() || !JeiItemLists.isFlavorAvailable(flavor)) continue;

            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.add(Ingredient.of(FlavorCompatibility.PLAIN_MOLASSES));
            ingredients.add(Ingredient.of(flavor.getEssenceItem()));

            recipes.add(new ShapelessRecipe(
                    new ResourceLocation(TobacconistMod.MODID, "jei/flavor_molasses/" + flavor.getFlavorPath()),
                    "", CraftingBookCategory.MISC, flavor.getStack(), ingredients));
        }
        return List.copyOf(recipes);
    }
}
