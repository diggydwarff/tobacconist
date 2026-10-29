package com.diggydwarff.tobacconistmod.backport;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.fluids.FluidStack;

/** Lossless typed access over ordinary Forge item/fluid NBT, including partial bottles. */
public final class ComponentData {
    private ComponentData() {}
    @SuppressWarnings("unchecked")
    public static <T> T get(ItemStack stack, DataComponentType<T> key) {
        if (stack.isEmpty()) return null;
        CompoundTag tag = stack.getTag();
        if (key == DataComponents.CUSTOM_NAME) return stack.hasCustomHoverName() ? (T)stack.getHoverName() : null;
        if (key == DataComponents.POTION_CONTENTS) return (T)new PotionContents(PotionUtils.getPotion(stack));
        if (key == DataComponents.DYED_COLOR) {
            if (tag == null || !tag.getCompound("display").contains("color", 99)) return null;
            return (T)new DyedItemColor(tag.getCompound("display").getInt("color"), true);
        }
        return decode(tag, key);
    }
    public static <T> T get(FluidStack stack, DataComponentType<T> key) { return decode(stack.getTag(), key); }
    private static <T> T decode(CompoundTag tag, DataComponentType<T> key) {
        return tag == null || !tag.contains(key.name) ? null : key.codec.parse(NbtOps.INSTANCE, tag.get(key.name)).result().orElse(null);
    }
    public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> key, T fallback) {
        T result = get(stack, key); return result == null ? fallback : result;
    }
    public static <T> T getOrDefault(FluidStack stack, DataComponentType<T> key, T fallback) {
        T result = get(stack, key); return result == null ? fallback : result;
    }
    public static <T> boolean has(ItemStack stack, DataComponentType<T> key) { return get(stack, key) != null; }
    public static <T> boolean has(FluidStack stack, DataComponentType<T> key) { return get(stack, key) != null; }
    public static <T> void set(ItemStack stack, DataComponentType<T> key, T value) {
        if (stack.isEmpty()) return;
        if (value == null) { remove(stack, key); return; }
        if (key == DataComponents.CUSTOM_NAME) { stack.setHoverName((Component)value); return; }
        if (key == DataComponents.DYED_COLOR) { stack.getOrCreateTagElement("display").putInt("color", ((DyedItemColor)value).rgb()); return; }
        if (key == DataComponents.POTION_CONTENTS) { PotionUtils.setPotion(stack, ((PotionContents)value).potion()); return; }
        stack.getOrCreateTag().put(key.name, key.codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, msg -> { throw new IllegalArgumentException(msg); }));
    }
    public static <T> void set(FluidStack stack, DataComponentType<T> key, T value) {
        if (stack.isEmpty()) return;
        if (value == null) { remove(stack, key); return; }
        stack.getOrCreateTag().put(key.name, key.codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, msg -> { throw new IllegalArgumentException(msg); }));
    }
    public static <T> void remove(ItemStack stack, DataComponentType<T> key) {
        if (key == DataComponents.CUSTOM_NAME) { stack.resetHoverName(); return; }
        if (key == DataComponents.DYED_COLOR) { if (stack.getTagElement("display") != null) stack.getTagElement("display").remove("color"); return; }
        if (stack.getTag() != null) stack.getTag().remove(key.name);
    }
    public static <T> void remove(FluidStack stack, DataComponentType<T> key) {
        if (stack.getTag() != null) stack.getTag().remove(key.name);
    }
}
