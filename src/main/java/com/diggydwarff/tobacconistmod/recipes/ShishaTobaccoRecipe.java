package com.diggydwarff.tobacconistmod.recipes;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.datagen.items.custom.FlavoringEssenceItem;
import com.diggydwarff.tobacconistmod.fluid.EssenceBottleFluidHandler;
import com.diggydwarff.tobacconistmod.util.TobaccoAromaticHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Essence lightly cases any loose tobacco into an aromatic. Full flavored-molasses bottles are the
 * heavy wet treatment that makes Shisha from any loose cut; unused Shisha may still take up to
 * three flavored-molasses applications.
 */
public class ShishaTobaccoRecipe extends CustomRecipe {
    public ShishaTobaccoRecipe(CraftingBookCategory category) { super(category); }

    private record Inputs(ItemStack source, BottledMolassesFlavors essence, List<BottledMolassesFlavors> molasses) {}

    @Override
    public boolean matches(CraftingInput input, Level level) {
        Inputs parsed = parse(input);
        if (parsed == null) return false;

        if (parsed.essence() != null) {
            return TobaccoCuringHelper.isLooseTobacco(parsed.source())
                    && TobaccoAromaticHelper.canAromatize(parsed.source());
        }

        if (TobaccoCuringHelper.isLooseTobacco(parsed.source())) {
            return !parsed.molasses().isEmpty() && parsed.molasses().size() <= 3
                    && TobaccoProcessingHelper.canMechanicallyMixToShisha(parsed.source());
        }
        return TobaccoProcessingHelper.canAddShishaFlavor(parsed.source())
                && TobaccoProcessingHelper.getShishaFlavorCount(parsed.source()) + parsed.molasses().size() <= 3;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Inputs parsed = parse(input);
        if (parsed == null) return ItemStack.EMPTY;

        if (parsed.essence() != null) {
            return TobaccoAromaticHelper.aromatize(parsed.source(), parsed.essence());
        }

        List<String> flavorNames = parsed.molasses().stream()
                .map(BottledMolassesFlavors::getShishaFlavorTag).toList();
        if (TobaccoCuringHelper.isLooseTobacco(parsed.source())) {
            return TobaccoProcessingHelper.createShisha(parsed.source(), flavorNames);
        }
        return TobaccoProcessingHelper.addShishaFlavors(parsed.source(), flavorNames);
    }

    private Inputs parse(CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        BottledMolassesFlavors essence = null;
        List<BottledMolassesFlavors> molasses = new ArrayList<>(3);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (TobaccoCuringHelper.isLooseTobacco(stack) || TobaccoProcessingHelper.isShisha(stack)) {
                if (!source.isEmpty()) return null;
                source = stack;
                continue;
            }

            if (stack.getItem() instanceof FlavoringEssenceItem) {
                if (essence != null || !molasses.isEmpty()) return null;
                essence = BottledMolassesFlavors.fromEssenceItem(stack.getItem());
                if (essence == null) return null;
                continue;
            }

            BottledMolassesFlavors flavor = BottledMolassesFlavors.fromItem(stack.getItem());
            if (flavor != null && !flavor.isPlain()) {
                if (essence != null || molasses.size() >= 3) return null;
                molasses.add(flavor);
                continue;
            }
            return null;
        }
        if (source.isEmpty() || (essence == null && molasses.isEmpty())) return null;
        return new Inputs(source, essence, List.copyOf(molasses));
    }

    @Override public boolean isSpecial() { return true; }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remains = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof FlavoringEssenceItem) {
                remains.set(i, new ItemStack(Items.GLASS_BOTTLE));
                continue;
            }

            BottledMolassesFlavors molasses = BottledMolassesFlavors.fromItem(stack.getItem());
            if (molasses != null && !molasses.isPlain()) {
                remains.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return remains;
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.SHISHA_TOBACCO_RECIPE_SERIALIZER.get(); }
}
