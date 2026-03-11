package com.diggydwarff.tobacconistmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TobaccoLabelHelper {

    public static final String TAG_BOX_LABEL = "BoxLabel";
    public static final String TAG_PRODUCT_LABEL = "ProductLabel";

    private TobaccoLabelHelper() {}

    public static String getBoxLabel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return "";
        return tag.getString(TAG_BOX_LABEL);
    }

    public static void setBoxLabel(ItemStack stack, String label) {
        if (label == null || label.isBlank()) return;
        stack.getOrCreateTag().putString(TAG_BOX_LABEL, label.trim());
    }

    public static void clearBoxLabel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        tag.remove(TAG_BOX_LABEL);

        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static String getProductLabel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return "";
        return tag.getString(TAG_PRODUCT_LABEL);
    }

    public static void setProductLabel(ItemStack stack, String label) {
        if (label == null || label.isBlank()) return;
        stack.getOrCreateTag().putString(TAG_PRODUCT_LABEL, label.trim());
    }

    public static void clearProductLabel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        tag.remove(TAG_PRODUCT_LABEL);

        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static boolean hasProductLabel(ItemStack stack) {
        return !getProductLabel(stack).isEmpty();
    }

    public static Component buildNamedProduct(String label, String suffix) {
        return Component.literal(label + " " + suffix);
    }
}