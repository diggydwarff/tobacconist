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
    /**
     * Plain molasses equivalents accepted by Tobacconist's dynamic flavoring recipe.
     * The tag always contains Tobacconist molasses and can be extended by optional mods/datapacks.
     */
    public static final TagKey<Item> PLAIN_MOLASSES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "plain_molasses")
    );

    private FlavorCompatibility() {
    }

    public static boolean isPlainMolasses(ItemStack stack) {
        return !stack.isEmpty() && stack.is(PLAIN_MOLASSES);
    }

    /**
     * Returns every currently resolved plain-molasses substitute for recipe displays.
     * Falls back to Tobacconist's own bottle if tags are not bound yet.
     */
    public static List<ItemStack> getPlainMolassesStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        BuiltInRegistries.ITEM.getTag(PLAIN_MOLASSES).ifPresent(holders ->
                holders.forEach(holder -> stacks.add(new ItemStack(holder.value())))
        );

        if (stacks.isEmpty()) {
            stacks.add(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getStack());
        }
        return List.copyOf(stacks);
    }

    /**
     * A flavor is usable only when its ingredient chain resolves to installed items.
     * This deliberately checks tags rather than mod IDs, so datapacks and other food mods can
     * satisfy the same flavor without adding hard dependencies.
     */
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

    private static boolean hasFlavoringIngredient(BottledMolassesFlavors flavor) {
        return BuiltInRegistries.ITEM.getTag(flavor.getFlavoringIngredientTag())
                .map(holders -> holders.size() > 0)
                .orElse(false);
    }
}
