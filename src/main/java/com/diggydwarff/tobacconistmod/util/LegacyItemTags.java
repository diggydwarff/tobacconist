package com.diggydwarff.tobacconistmod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility bridge for the mod's 1.20.1 per-stack NBT data.
 * Minecraft 1.20.5+ stores arbitrary item data in minecraft:custom_data.
 *
 * The live tag returned by getOrCreateTag() writes mutations back into the
 * CUSTOM_DATA component while exposing CompoundTag-style access
 * while the port is completed.
 */
public final class LegacyItemTags {
    private LegacyItemTags() {}

    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null || data.isEmpty() ? null : data.copyTag();
    }

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CompoundTag existing = getTag(stack);
        return new LiveTag(stack, existing == null ? new CompoundTag() : existing);
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    private static final class LiveTag extends CompoundTag {
        private final ItemStack owner;
        private boolean initializing = true;

        private LiveTag(ItemStack owner, CompoundTag source) {
            this.owner = owner;
            super.merge(source);
            this.initializing = false;
        }

        private void commit() {
            if (!initializing) {
                LegacyItemTags.setTag(owner, this);
            }
        }

        @Override
        public Tag put(String key, Tag value) {
            Tag previous = super.put(key, value);
            commit();
            return previous;
        }

        @Override
        public void putString(String key, String value) {
            super.putString(key, value);
            commit();
        }

        @Override
        public void putInt(String key, int value) {
            super.putInt(key, value);
            commit();
        }

        @Override
        public void putLong(String key, long value) {
            super.putLong(key, value);
            commit();
        }

        @Override
        public void putBoolean(String key, boolean value) {
            super.putBoolean(key, value);
            commit();
        }

        @Override
        public void remove(String key) {
            super.remove(key);
            commit();
        }

        @Override
        public CompoundTag merge(CompoundTag other) {
            CompoundTag result = super.merge(other);
            commit();
            return result;
        }
    }
}
