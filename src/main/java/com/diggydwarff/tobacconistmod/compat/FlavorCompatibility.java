package com.diggydwarff.tobacconistmod.compat;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Shared, tag-driven compatibility checks for optional food/flavor mods. */
public final class FlavorCompatibility {
    /** Plain molasses equivalents accepted by Tobacconist's dynamic flavoring recipe. */
    public static final TagKey<Item> PLAIN_MOLASSES = TagKey.create(
            Registries.ITEM, new ResourceLocation(TobacconistMod.MODID, "plain_molasses"));

    private FlavorCompatibility() {}

    public static boolean isPlainMolasses(ItemStack stack) {
        return !stack.isEmpty() && stack.is(PLAIN_MOLASSES);
    }

    public static List<ItemStack> getPlainMolassesStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        BuiltInRegistries.ITEM.getTag(PLAIN_MOLASSES).ifPresent(holders ->
                holders.forEach(holder -> stacks.add(new ItemStack(holder.value()))));
        if (stacks.isEmpty()) {
            stacks.add(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getStack());
        }
        return List.copyOf(stacks);
    }

    /** A flavor is visible/usable only when the ingredient chain resolves to installed items. */
    public static boolean isFlavorAvailable(BottledMolassesFlavors flavor) {
        if (flavor.isPlain()) return true;
        return switch (flavor) {
            case BOTTLED_MOLASSES_TWO_APPLES_FLAVOR ->
                    hasFlavoringIngredient(BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR);
            case BOTTLED_MOLASSES_TWO_GOLDENAPPLES_FLAVOR ->
                    hasFlavoringIngredient(BottledMolassesFlavors.BOTTLED_MOLASSES_GOLDENAPPLE_FLAVOR);
            case BOTTLED_MOLASSES_TWO_ROYALAPPLES_FLAVOR ->
                    hasFlavoringIngredient(BottledMolassesFlavors.BOTTLED_MOLASSES_ROYALAPPLE_FLAVOR);
            case BOTTLED_MOLASSES_BERRY_FLAVOR ->
                    hasFlavoringIngredient(BottledMolassesFlavors.BOTTLED_MOLASSES_SWEETBERRY_FLAVOR)
                            && hasFlavoringIngredient(BottledMolassesFlavors.BOTTLED_MOLASSES_GLOWBERRY_FLAVOR);
            default -> hasFlavoringIngredient(flavor);
        };
    }

    public static boolean hasFlavoringIngredient(BottledMolassesFlavors flavor) {
        return BuiltInRegistries.ITEM.getTag(flavor.getFlavoringIngredientTag())
                .map(holders -> holders.size() > 0)
                .orElse(false);
    }
}
