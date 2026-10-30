package com.diggydwarff.tobacconistmod.backport;

import net.minecraft.world.item.ItemStack;

public final class BackportStacks {
    private BackportStacks() {}
    public static ItemStack copyWithCount(ItemStack source, int count) {
        ItemStack copy = source.copy();
        copy.setCount(count);
        return copy;
    }
    public static boolean isSameItem(ItemStack first, ItemStack second) {
        return first.getItem() == second.getItem();
    }
}
