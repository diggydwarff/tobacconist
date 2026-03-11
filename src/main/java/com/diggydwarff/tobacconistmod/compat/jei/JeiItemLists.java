package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LabelItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.ShishaFlavoringItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
import com.diggydwarff.tobacconistmod.util.TobaccoBoxHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public final class JeiItemLists {

    private JeiItemLists() {}

    public static List<ItemStack> getAllSmokingPipes() {
        return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .filter(item -> item instanceof WoodenSmokingPipeItem)
                .map(ItemStack::new)
                .toList();
    }

    public static List<ItemStack> getAllLooseTobaccos() {
        List<ItemStack> out = new ArrayList<>();

        out.add(makeLoose(ModItems.TOBACCO_LOOSE_WILD.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_VIRGINIA.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_BURLEY.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_ORIENTAL.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_DOKHA.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_SHADE.get()));

        return out;
    }

    public static List<ItemStack> getAllShishaFlavorings() {
        return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .filter(item -> item instanceof ShishaFlavoringItem)
                .map(ItemStack::new)
                .toList();
    }

    public static List<ItemStack> getAllTobaccoBoxSupportedContents() {
        List<ItemStack> out = new ArrayList<>();

        out.add(makeLoose(ModItems.TOBACCO_LOOSE_WILD.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_VIRGINIA.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_BURLEY.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_ORIENTAL.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_DOKHA.get()));
        out.add(makeLoose(ModItems.TOBACCO_LOOSE_SHADE.get()));

        out.add(makeCigarette());
        out.add(makeCigar());
        out.add(makeShisha());

        return out;
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

    private static ItemStack makeLoose(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        TobaccoCuringHelper.applyCureData(stack, TobaccoCuringHelper.CURE_AIR, 60);
        TobaccoCuringHelper.setCutType(stack, TobaccoCuringHelper.CUT_RIBBON);
        return stack;
    }

    private static ItemStack makeCigarette() {
        ItemStack stack = new ItemStack(ModItems.CIGARETTE.get());
        stack.getOrCreateTag().putString("tobacco", "Virginia");
        stack.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        return stack;
    }

    private static ItemStack makeCigar() {
        ItemStack stack = new ItemStack(ModItems.CIGAR.get());
        stack.getOrCreateTag().putString("tobacco", "Burley");
        stack.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        return stack;
    }

    private static ItemStack makeShisha() {
        ItemStack stack = new ItemStack(ModItems.SHISHA_TOBACCO.get());
        stack.getOrCreateTag().putString("tobacco", "Oriental");
        stack.getOrCreateTag().putString("flavor1", "Molasses");
        stack.getOrCreateTag().putInt(TobaccoCuringHelper.TAG_QUALITY, 60);
        stack.getOrCreateTag().putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(60)
        );
        return stack;
    }
}