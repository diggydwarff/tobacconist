package com.diggydwarff.tobacconistmod.block.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** Standard one-slot capability view used by Create funnels, chutes and Mechanical Arms. */
final class TobaccoBarrelItemHandler implements IItemHandler {
    private final TobaccoBarrelBlockEntity barrel;

    TobaccoBarrelItemHandler(TobaccoBarrelBlockEntity barrel) {
        this.barrel = barrel;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? barrel.getStoredTobacco() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty()) {
            return stack;
        }

        int toInsert = barrel.getInsertableAmount(stack);
        if (toInsert <= 0) {
            return stack;
        }

        if (!simulate) {
            toInsert = barrel.tryInsertTobaccoAutomated(stack);
        }

        if (toInsert >= stack.getCount()) {
            return ItemStack.EMPTY;
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(toInsert);
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        return barrel.extractTobaccoAutomated(amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? TobaccoBarrelBlockEntity.MAX_STACK : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && barrel.getInsertableAmount(stack) > 0;
    }
}
