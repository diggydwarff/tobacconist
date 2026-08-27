package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** JEI-only examples for the dynamic plain-molasses + essence crafting recipe. */
public record FlavorMolassesJeiRecipe(
        ItemStack plainMolasses,
        ItemStack essence,
        ItemStack output
) {
    public static List<FlavorMolassesJeiRecipe> createAll() {
        List<FlavorMolassesJeiRecipe> recipes = new ArrayList<>();
        ItemStack plain = BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getStack();

        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.isPlain() || !JeiItemLists.isFlavorAvailable(flavor)) continue;
            recipes.add(new FlavorMolassesJeiRecipe(
                    plain.copy(),
                    flavor.getEssenceStack(),
                    flavor.getStack()
            ));
        }

        return List.copyOf(recipes);
    }
}
