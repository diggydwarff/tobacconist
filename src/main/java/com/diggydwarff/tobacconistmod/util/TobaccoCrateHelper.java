package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Lossless NBT/component storage helpers for the 9-item tobacco crates. */
public final class TobaccoCrateHelper {
    public static final String TAG_CONTENTS = "TobaccoCrateContents";
    public static final int CAPACITY = 9;

    private TobaccoCrateHelper() {}

    public static boolean isCrateableTobacco(ItemStack stack) {
        return TobaccoCuringHelper.isRawTobaccoLeaf(stack)
                || TobaccoCuringHelper.isDryTobaccoLeaf(stack)
                || TobaccoCuringHelper.isLooseTobacco(stack);
    }

    /** Crate inputs must share the same registry item; stack data may differ. */
    public static boolean sameTobaccoType(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && first.getItem() == second.getItem();
    }

    public static ItemStack createCrate(ItemStack tobacco, List<ItemStack> contents) {
        Item crateItem = getCrateItem(tobacco);
        if (crateItem == null || contents.size() != CAPACITY) return ItemStack.EMPTY;

        ItemStack crate = new ItemStack(crateItem);
        CompoundTag tag = LegacyItemTags.getTag(crate);
        if (tag == null) tag = new CompoundTag();

        ListTag list = new ListTag();
        for (ItemStack source : contents) {
            ItemStack one = source.copyWithCount(1);
            list.add(one.save(new CompoundTag()));
        }
        tag.put(TAG_CONTENTS, list);
        LegacyItemTags.setTag(crate, tag);
        return crate;
    }

    public static List<ItemStack> readContents(ItemStack crate) {
        CompoundTag tag = LegacyItemTags.getTag(crate);
        if (tag == null || !tag.contains(TAG_CONTENTS, Tag.TAG_LIST)) return List.of();

        ListTag list = tag.getList(TAG_CONTENTS, Tag.TAG_COMPOUND);
        List<ItemStack> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ItemStack parsed = ItemStack.of(list.getCompound(i));
            if (!parsed.isEmpty()) result.add(parsed);
        }
        return result;
    }

    private static Item getCrateItem(ItemStack stack) {
        Item item = stack.getItem();

        if (item == ModItems.VIRGINIA_TOBACCO_LEAF.get()) {
            return ModBlocks.RAW_VIRGINIA_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()
                || item == ModItems.TOBACCO_LOOSE_VIRGINIA.get()) {
            return ModBlocks.VIRGINIA_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.BURLEY_TOBACCO_LEAF.get()) {
            return ModBlocks.RAW_BURLEY_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.BURLEY_TOBACCO_LEAF_DRY.get()
                || item == ModItems.TOBACCO_LOOSE_BURLEY.get()) {
            return ModBlocks.BURLEY_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.ORIENTAL_TOBACCO_LEAF.get()) {
            return ModBlocks.RAW_ORIENTAL_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.ORIENTAL_TOBACCO_LEAF_DRY.get()
                || item == ModItems.TOBACCO_LOOSE_ORIENTAL.get()) {
            return ModBlocks.ORIENTAL_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.DOKHA_TOBACCO_LEAF.get()) {
            return ModBlocks.RAW_DOKHA_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.DOKHA_TOBACCO_LEAF_DRY.get()
                || item == ModItems.TOBACCO_LOOSE_DOKHA.get()) {
            return ModBlocks.DOKHA_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.SHADE_TOBACCO_LEAF.get()) {
            return ModBlocks.RAW_SHADE_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.SHADE_TOBACCO_LEAF_DRY.get()
                || item == ModItems.TOBACCO_LOOSE_SHADE.get()) {
            return ModBlocks.SHADE_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.WILD_TOBACCO_LEAF.get()) {
            return ModBlocks.RAW_WILD_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.WILD_TOBACCO_LEAF_DRY.get()
                || item == ModItems.TOBACCO_LOOSE_WILD.get()) {
            return ModBlocks.WILD_TOBACCO_CRATE.get().asItem();
        }
        if (item == ModItems.BLENDED_TOBACCO.get()) {
            return ModBlocks.BLENDED_TOBACCO_CRATE.get().asItem();
        }
        return null;
    }
}
