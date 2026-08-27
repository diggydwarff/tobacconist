package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LabelItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public final class JeiItemLists {

    private static final List<String> LOOSE_CUTS = List.of(
            TobaccoCuringHelper.CUT_ROUGH,
            TobaccoCuringHelper.CUT_RIBBON,
            TobaccoCuringHelper.CUT_SHAG,
            TobaccoCuringHelper.CUT_FLAKE
    );

    private JeiItemLists() {}

    public static List<ItemStack> getAllSmokingPipes() {
        return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .filter(item -> item instanceof WoodenSmokingPipeItem)
                .map(ItemStack::new)
                .toList();
    }

    /** All JEI-visible loose tobacco variants, including cut metadata used by subtype lookup. */
    public static List<ItemStack> getAllLooseTobaccos() {
        List<ItemStack> out = new ArrayList<>();

        addAllCuts(out, ModItems.TOBACCO_LOOSE_WILD.get());
        addAllCuts(out, ModItems.TOBACCO_LOOSE_VIRGINIA.get());
        addAllCuts(out, ModItems.TOBACCO_LOOSE_BURLEY.get());
        addAllCuts(out, ModItems.TOBACCO_LOOSE_ORIENTAL.get());
        addAllCuts(out, ModItems.TOBACCO_LOOSE_DOKHA.get());
        addAllCuts(out, ModItems.TOBACCO_LOOSE_SHADE.get());
        addAllCuts(out, ModItems.BLENDED_TOBACCO.get());
        return List.copyOf(out);
    }

    public static List<ItemStack> getAllCuts(Item item) {
        List<ItemStack> out = new ArrayList<>(LOOSE_CUTS.size());
        addAllCuts(out, item);
        return List.copyOf(out);
    }

    public static List<ItemStack> getAllShishaFlavorings() {
        List<ItemStack> out = new ArrayList<>();
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (!flavor.isPlain() && isFlavorAvailable(flavor)) {
                out.add(flavor.getStack());
            }
        }
        return List.copyOf(out);
    }

    /**
     * Hide registered flavor bottles/essences whose ingredient tag has no installed provider.
     * Registration stays intact for world compatibility; this only removes unusable entries from JEI.
     */
    public static List<ItemStack> getUnavailableFlavorItems() {
        List<ItemStack> out = new ArrayList<>();
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.isPlain() || isFlavorAvailable(flavor)) continue;
            out.add(flavor.getStack());
            ItemStack essence = flavor.getEssenceStack();
            if (!essence.isEmpty()) out.add(essence);
        }
        return List.copyOf(out);
    }

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
        TagKey<Item> tag = flavor.getFlavoringIngredientTag();
        return BuiltInRegistries.ITEM.getTag(tag)
                .map(holders -> holders.size() > 0)
                .orElse(false);
    }

    public static List<ItemStack> getAllTobaccoBoxSupportedContents() {
        List<ItemStack> out = new ArrayList<>(getAllLooseTobaccos());

        out.add(makeCigarette());
        out.add(makeCigar());
        out.add(makeShisha());

        return List.copyOf(out);
    }

    public static ItemStack makeNamedLabel(String name) {
        ItemStack stack = new ItemStack(ModItems.TOBACCO_LABEL.get());
        LabelItem.setLabelName(stack, name);
        return stack;
    }

    public static ItemStack makeBlankLabel() {
        return new ItemStack(ModItems.TOBACCO_LABEL.get());
    }

    public static ItemStack makeFilledBox(ItemStack stored, int count) {
        ItemStack box = new ItemStack(ModItems.TOBACCO_BOX.get());
        TobaccoBoxHelper.setStored(box, stored, count);
        return box;
    }

    public static ItemStack makeLabeledBox(ItemStack stored, int count, String label) {
        ItemStack box = makeFilledBox(stored, count);
        TobaccoBoxHelper.setLabel(box, label);
        return box;
    }

    private static void addAllCuts(List<ItemStack> out, Item item) {
        for (String cut : LOOSE_CUTS) {
            out.add(makeLoose(item, cut));
        }
    }

    private static ItemStack makeLoose(Item item, String cut) {
        ItemStack stack = new ItemStack(item);
        TobaccoCuringHelper.applyCureData(stack, TobaccoCuringHelper.CURE_AIR, 60);
        TobaccoCuringHelper.setCutType(stack, cut);
        return stack;
    }

    private static ItemStack makeCigarette() {
        ItemStack stack = new ItemStack(ModItems.CIGARETTE.get());
        LegacyItemTags.getOrCreateTag(stack).putString("tobacco", "Virginia");
        LegacyItemTags.getOrCreateTag(stack).putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        return stack;
    }

    private static ItemStack makeCigar() {
        ItemStack stack = new ItemStack(ModItems.CIGAR.get());
        LegacyItemTags.getOrCreateTag(stack).putString("tobacco", "Burley");
        LegacyItemTags.getOrCreateTag(stack).putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        return stack;
    }

    private static ItemStack makeShisha() {
        ItemStack stack = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        LegacyItemTags.getOrCreateTag(stack).putString("tobacco", "Oriental");
        LegacyItemTags.getOrCreateTag(stack).putString("flavor1", "Molasses");
        LegacyItemTags.getOrCreateTag(stack).putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        LegacyItemTags.getOrCreateTag(stack).putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(60)
        );
        return stack;
    }

}