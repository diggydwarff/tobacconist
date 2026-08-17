package com.diggydwarff.tobacconistmod.block.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.IItemHandler;

/** Fuel-only capability adapter for the flue firebox. */
final class FlueFireboxItemHandler implements IItemHandler {
    private final FlueFireboxBlockEntity firebox;

    FlueFireboxItemHandler(FlueFireboxBlockEntity firebox) {
        this.firebox = firebox;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? firebox.getItem(0) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty() || stack.getBurnTime(RecipeType.SMELTING) <= 0) {
            return stack;
        }

        ItemStack stored = firebox.getItem(0);
        if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, stack)) {
            return stack;
        }

        int current = stored.isEmpty() ? 0 : stored.getCount();
        int itemLimit = stored.isEmpty() ? stack.getMaxStackSize() : stored.getMaxStackSize();
        int slotLimit = Math.min(firebox.getMaxStackSize(), itemLimit);
        int space = Math.max(0, slotLimit - current);
        int move = Math.min(space, stack.getCount());
        if (move <= 0) return stack;

        if (!simulate) {
            if (stored.isEmpty()) {
                firebox.setItem(0, stack.copyWithCount(move));
            } else {
                ItemStack updated = stored.copy();
                updated.grow(move);
                firebox.setItem(0, updated);
            }
        }

        if (move >= stack.getCount()) return ItemStack.EMPTY;
        ItemStack remainder = stack.copy();
        remainder.shrink(move);
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? firebox.getMaxStackSize() : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && !stack.isEmpty() && stack.getBurnTime(RecipeType.SMELTING) > 0;
    }
}
