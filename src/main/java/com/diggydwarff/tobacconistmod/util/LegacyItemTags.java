package com.diggydwarff.tobacconistmod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Direct bridge to 1.20.1 ItemStack NBT. */
public final class LegacyItemTags {
    private LegacyItemTags() {}

    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        return stack.getTag();
    }

    public static boolean hasTag(ItemStack stack) {
        return stack.hasTag();
    }

    public static CompoundTag getOrCreateTag(ItemStack stack) {
        return stack.getOrCreateTag();
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        stack.setTag(tag);
    }
}
