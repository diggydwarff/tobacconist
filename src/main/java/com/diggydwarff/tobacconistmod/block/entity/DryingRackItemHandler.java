package com.diggydwarff.tobacconistmod.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Forge item capability adapter for the drying rack.
 *
 * <p>Horizontal faces are inputs on both rack tiers. The wooden rack keeps top and bottom insertion
 * closed, while the industrial rack permits top-down loading. Finished batches may be extracted
 * through any capability face. This is intentional: capability-driven logistics such
 * as Create funnels may query the attached inventory through a different logical face than vanilla
 * sided-container automation. The rack's finished-state check remains authoritative, so automation
 * still cannot pull an active curing batch.</p>
 */
final class DryingRackItemHandler implements IItemHandler {
    private final TobaccoDryingRackBlockEntity rack;
    @Nullable
    private final Direction side;

    DryingRackItemHandler(TobaccoDryingRackBlockEntity rack, @Nullable Direction side) {
        this.rack = rack;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? rack.getStoredLeaf() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty() || !canInsertFromSide() || !rack.canAccept(stack)) {
            return stack;
        }

        int space = rack.getMaxLeaves() - rack.getLeafCount();
        int toInsert = Math.min(space, stack.getCount());
        if (toInsert <= 0) {
            return stack;
        }

        if (!simulate) {
            for (int i = 0; i < toInsert; i++) {
                if (!rack.addOneLeaf(stack)) {
                    toInsert = i;
                    break;
                }
            }
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
        if (slot != 0 || amount <= 0 || !canExtractFromSide() || !rack.isFinished()) {
            return ItemStack.EMPTY;
        }

        ItemStack stored = rack.getStoredLeaf();
        if (stored.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, stored.getCount());
        if (simulate) {
            return stored.copyWithCount(extracted);
        }

        return rack.removeItem(0, extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? rack.getMaxLeaves() : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && canInsertFromSide() && rack.canAccept(stack);
    }

    private boolean canInsertFromSide() {
        if (side == null) return true;
        if (rack instanceof IndustrialDryingRackBlockEntity) {
            // The industrial rack is a true two-block machine: horizontal automation can address
            // either level, and Chutes/Hoppers above feed through the upper half's top face.
            return side != Direction.DOWN;
        }
        return side != Direction.UP && side != Direction.DOWN;
    }

    private boolean canExtractFromSide() {
        // Vanilla hoppers still obey WorldlyContainer's bottom-only extraction rules.
        // Forge capability consumers (including Create funnels) are allowed to pull a
        // finished batch from whichever face they use to address the rack.
        return true;
    }
}
