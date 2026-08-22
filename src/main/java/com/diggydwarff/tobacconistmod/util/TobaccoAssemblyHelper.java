package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Metadata carrier for staged Create Deployer -> Press cigarette/cigar assembly. */
public final class TobaccoAssemblyHelper {
    private static final String TAG_FILLER_ITEM = "AssemblyFillerItem";
    private static final String TAG_FILLER_DATA = "AssemblyFillerData";
    private static final String TAG_WRAPPER_ITEM = "AssemblyWrapperItem";
    private static final String TAG_WRAPPER_DATA = "AssemblyWrapperData";

    private TobaccoAssemblyHelper() {}

    public static ItemStack makeIncompleteCigarette(ItemStack filler) {
        if (!TobaccoCuringHelper.isLooseTobacco(filler)) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(ModItems.INCOMPLETE_CIGARETTE.get());
        writeStack(result, filler, TAG_FILLER_ITEM, TAG_FILLER_DATA);
        return result;
    }

    public static ItemStack makeIncompleteCigar(ItemStack filler, ItemStack wrapper) {
        if (!TobaccoCuringHelper.isLooseTobacco(filler) || !TobaccoCuringHelper.isDryTobaccoLeaf(wrapper)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(ModItems.INCOMPLETE_CIGAR.get());
        writeStack(result, filler, TAG_FILLER_ITEM, TAG_FILLER_DATA);
        writeStack(result, wrapper, TAG_WRAPPER_ITEM, TAG_WRAPPER_DATA);
        return result;
    }

    public static ItemStack finish(ItemStack incomplete) {
        if (incomplete.is(ModItems.INCOMPLETE_CIGARETTE.get())) {
            ItemStack filler = readStack(incomplete, TAG_FILLER_ITEM, TAG_FILLER_DATA);
            return TobaccoProductCraftingHelper.makeCigarette(filler);
        }
        if (incomplete.is(ModItems.INCOMPLETE_CIGAR.get())) {
            ItemStack filler = readStack(incomplete, TAG_FILLER_ITEM, TAG_FILLER_DATA);
            ItemStack wrapper = readStack(incomplete, TAG_WRAPPER_ITEM, TAG_WRAPPER_DATA);
            return TobaccoProductCraftingHelper.makeCigar(filler, wrapper);
        }
        return ItemStack.EMPTY;
    }

    private static void writeStack(ItemStack target, ItemStack source, String itemKey, String dataKey) {
        CompoundTag targetTag = LegacyItemTags.getOrCreateTag(target);
        targetTag.putString(itemKey, BuiltInRegistries.ITEM.getKey(source.getItem()).toString());
        CompoundTag sourceData = LegacyItemTags.getTag(source);
        if (sourceData != null && !sourceData.isEmpty()) {
            targetTag.put(dataKey, sourceData.copy());
        }
    }

    private static ItemStack readStack(ItemStack source, String itemKey, String dataKey) {
        CompoundTag tag = LegacyItemTags.getTag(source);
        if (tag == null || !tag.contains(itemKey)) return ItemStack.EMPTY;

        ResourceLocation id = ResourceLocation.tryParse(tag.getString(itemKey));
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(item);
        if (tag.contains(dataKey)) {
            LegacyItemTags.setTag(result, tag.getCompound(dataKey).copy());
        }
        return result;
    }
}
