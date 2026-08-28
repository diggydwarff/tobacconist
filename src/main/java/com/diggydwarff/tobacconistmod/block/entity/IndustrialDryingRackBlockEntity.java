package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.custom.IndustrialDryingRackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.BlockState;

/**
 * High-throughput Create-only drying rack. It reuses the traditional rack's curing metadata and
 * quality rules, but doubles batch capacity and only advances when both rack tiers receive
 * matching Create airflow from distinct Encased Fans.
 */
public class IndustrialDryingRackBlockEntity extends TobaccoDryingRackBlockEntity {
    public static final int INDUSTRIAL_MAX_LEAVES = 32;

    public IndustrialDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(), pos, state);
    }


    public IndustrialDryingRackBlockEntity getMasterRack() {
        if (level == null || !getBlockState().hasProperty(IndustrialDryingRackBlock.HALF)
                || getBlockState().getValue(IndustrialDryingRackBlock.HALF) == DoubleBlockHalf.LOWER) {
            return this;
        }
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        return below instanceof IndustrialDryingRackBlockEntity rack ? rack : this;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getItemHandler(side) : master.getItemHandler(side);
    }

    @Override
    public int getContainerSize() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getContainerSize() : master.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.isEmpty() : master.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getItem(slot) : master.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.removeItem(slot, amount) : master.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.removeItemNoUpdate(slot) : master.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        if (master == this) {
            super.setItem(slot, stack);
        } else {
            master.setItem(slot, stack);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        if (master != this) return master.getSlotsForFace(side);
        // Industrial automation follows the shared rack rule: input from the top or horizontal sides; bottom is output-only.
        return side == Direction.DOWN ? new int[]{0} : new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        if (master != this) return master.canPlaceItemThroughFace(slot, stack, side);
        if (side == Direction.DOWN) return false;
        return getItemHandler(side).isItemValid(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this
                ? super.canTakeItemThroughFace(slot, stack, side)
                : master.canTakeItemThroughFace(slot, stack, side);
    }

    @Override
    public void clearContent() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        if (master == this) {
            super.clearContent();
        } else {
            master.clearContent();
        }
    }

    @Override
    public ItemStack getStoredLeaf() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getStoredLeaf() : master.getStoredLeaf();
    }

    @Override
    public boolean hasLeaves() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.hasLeaves() : master.hasLeaves();
    }

    @Override
    public int getLeafCount() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getLeafCount() : master.getLeafCount();
    }

    @Override
    public int getDryProgressPercent() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getDryProgressPercent() : master.getDryProgressPercent();
    }

    @Override
    public int getEstimatedTicksRemaining() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getEstimatedTicksRemaining() : master.getEstimatedTicksRemaining();
    }

    @Override
    public MutableComponent getRackStatusComponent() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getRackStatusComponent() : master.getRackStatusComponent();
    }

    @Override
    public MutableComponent getCurrentCureMethodComponent() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.getCurrentCureMethodComponent() : master.getCurrentCureMethodComponent();
    }

    @Override
    public boolean isDryingActive() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.isDryingActive() : master.isDryingActive();
    }

    @Override
    public boolean isFinished() {
        IndustrialDryingRackBlockEntity master = getMasterRack();
        return master == this ? super.isFinished() : master.isFinished();
    }

    @Override
    public int getMaxLeaves() {
        return INDUSTRIAL_MAX_LEAVES;
    }

    @Override
    public boolean requiresCreateAssistance() {
        return true;
    }

    @Override
    protected int adjustCreateAssistedTickRate(int baseRate) {
        // +1 curing tick per game tick is intentionally modest: plain fan Air/Sun goes 4 -> 5
        // (~20% less wall-clock time), while heated Fire/Flue goes 6 -> 7 (~14% less time).
        return baseRate + 1;
    }

    @Override
    public int getVisualLoadStage() {
        int count = Math.min(getMaxLeaves(), getLeafCount());
        if (count <= 0) return 0;
        if (count <= 10) return 1;
        if (count <= 21) return 2;
        return 3;
    }
}
