package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TobaccoLabelHelper {

    public static final String TAG_BOX_LABEL = "BoxLabel";
    public static final String TAG_PRODUCT_LABEL = "ProductLabel";

    private TobaccoLabelHelper() {}

    public static String getBoxLabel(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) return "";
        return tag.getString(TAG_BOX_LABEL);
    }

    public static void setBoxLabel(ItemStack stack, String label) {
        if (label == null || label.isBlank()) return;
        LegacyItemTags.getOrCreateTag(stack).putString(TAG_BOX_LABEL, label.trim());
    }

    public static void clearBoxLabel(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) return;

        tag.remove(TAG_BOX_LABEL);

        if (tag.isEmpty()) {
            LegacyItemTags.setTag(stack, null);
        } else {
            LegacyItemTags.setTag(stack, tag);
        }
    }

    public static String getProductLabel(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) return "";
        return tag.getString(TAG_PRODUCT_LABEL);
    }

    public static void setProductLabel(ItemStack stack, String label) {
        if (label == null || label.isBlank()) return;
        LegacyItemTags.getOrCreateTag(stack).putString(TAG_PRODUCT_LABEL, label.trim());
    }

    public static void clearProductLabel(ItemStack stack) {
        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (tag == null) return;

        tag.remove(TAG_PRODUCT_LABEL);

        if (tag.isEmpty()) {
            LegacyItemTags.setTag(stack, null);
        } else {
            LegacyItemTags.setTag(stack, tag);
        }
    }

    public static boolean hasProductLabel(ItemStack stack) {
        return !getProductLabel(stack).isEmpty();
    }

    public static Component buildNamedProduct(String label, String suffix) {
        return Component.translatable("tobacconistmod.product.named", label, suffix);
    }

    public static Component buildNamedProduct(String label, Component suffix) {
        return Component.translatable("tobacconistmod.product.named", label, suffix);
    }
}