package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Configuration-only menu: the filter slot is a ghost setting, not an inventory. */
public class ProductionMonitorMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos blockPos;
    private final ContainerData data;
    private ItemStack initialFilter;

    public ProductionMonitorMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, readOpenData(buf));
    }

    private ProductionMonitorMenu(int id, Inventory inventory, OpenData openData) {
        this(id, inventory, openData.blockPos(), bootstrapData(openData), openData.filter());
    }

    public ProductionMonitorMenu(int id, Inventory inventory, ProductionMonitorBlockEntity blockEntity,
                                 ContainerData data, ItemStack initialFilter) {
        this(id, inventory, blockEntity.getBlockPos(), data, initialFilter);
    }

    private static OpenData readOpenData(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        long count = buf.readLong();
        int target = buf.readVarInt();
        int countMode = buf.readUnsignedByte();
        int atTarget = buf.readUnsignedByte();
        int output = buf.readUnsignedByte();
        boolean externalReset = buf.readBoolean();
        int rollingRateHundredths = buf.readVarInt();
        boolean targetValid = buf.readBoolean();
        ItemStack filter = buf.readItem();
        return new OpenData(pos, count, target, countMode, atTarget, output, externalReset,
                rollingRateHundredths, targetValid, filter);
    }

    private static ContainerData bootstrapData(OpenData openData) {
        SimpleContainerData data = new SimpleContainerData(9);
        data.set(0, (int) openData.count());
        data.set(1, (int) (openData.count() >>> 32));
        data.set(2, openData.target());
        data.set(3, openData.countMode());
        data.set(4, openData.atTarget());
        data.set(5, openData.output());
        data.set(6, openData.externalReset() ? 1 : 0);
        data.set(7, openData.rollingRateHundredths());
        data.set(8, openData.targetValid() ? 1 : 0);
        return data;
    }

    private ProductionMonitorMenu(int id, Inventory inventory, BlockPos blockPos,
                                  ContainerData data, ItemStack initialFilter) {
        super(ModMenuTypes.PRODUCTION_MONITOR_MENU.get(), id);
        this.level = inventory.player.level();
        this.blockPos = blockPos;
        this.data = data;
        this.initialFilter = initialFilter == null ? ItemStack.EMPTY : initialFilter.copy();
        addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public long getCount() {
        return ((long) data.get(1) << 32) | ((long) data.get(0) & 0xFFFFFFFFL);
    }

    public int getTarget() { return Math.max(1, data.get(2)); }
    public int getCountModeOrdinal() { return data.get(3); }
    public int getAtTargetOrdinal() { return data.get(4); }
    public int getOutputOrdinal() { return data.get(5); }
    public boolean isExternalResetEnabled() { return data.get(6) != 0; }
    public double getRollingRate() { return Math.max(0, data.get(7)) / 100.0D; }
    public boolean isTargetValid() { return data.get(8) != 0; }
    public ItemStack getInitialFilter() { return initialFilter.copy(); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!level.getBlockState(blockPos).is(ModBlocks.PRODUCTION_MONITOR.get())) return false;
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    private record OpenData(BlockPos blockPos, long count, int target, int countMode, int atTarget, int output,
                            boolean externalReset, int rollingRateHundredths, boolean targetValid, ItemStack filter) {}
}
