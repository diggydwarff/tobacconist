package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class LiquidStationMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final BlockPos pos;
    private final boolean blending;
    private final ContainerData data;

    public LiquidStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), buffer.readBoolean(), new ItemStackHandler(11), new SimpleContainerData(13));
    }
    public LiquidStationMenu(int id, Inventory inventory, LiquidStationEntity station) {
        this(id, inventory, station.getBlockPos(), station.isBlending(), station.inventory, station.data);
    }
    private LiquidStationMenu(int id, Inventory inventory, BlockPos pos, boolean blending, ItemStackHandler items, ContainerData data) {
        super(VaporContent.STATION_MENU.get(), id); this.playerInventory = inventory;
        this.pos = pos; this.blending = blending; this.data = data;
        for (int slot = 0; slot < 11; slot++) {
            int x, y;
            if (slot < 6) { x = 17 + (slot % 3) * 62; y = slot < 3 ? 42 : 80; }
            else { x = switch (slot) { case 6 -> 17; case 7 -> 141; case 8 -> 79; case 9 -> 79; default -> 203; }; y = slot == 9 ? 80 : 118; }
            final int index = slot;
            addSlot(new SlotItemHandler(items, slot, x, y) {
                @Override public boolean isActive() { return blending ? index != 9 : index < 3 || index == 7 || index == 9 || index == 10; }
                @Override public boolean mayPlace(ItemStack stack) {
                    return index != 7 && index != 10 && super.mayPlace(stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, 9 + row * 9 + col, 44 + col * 18, 210 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 44 + col * 18, 268));
        addDataSlots(data);
    }
    public boolean blending() { return blending; }
    public int value(int index) { return data.get(index); }
    public BlockPos pos() { return pos; }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id > 8 || !stillValid(player)) return false;
        if (player.level().getBlockEntity(pos) instanceof LiquidStationEntity station) { station.button(id); return true; }
        return false;
    }
    @Override public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof LiquidStationEntity
                && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem(), copy = original.copy();
        if (index < 11) {
            if (!moveItemStackTo(original, 11, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < 11 && !original.isEmpty(); i++) {
                Slot target = slots.get(i);
                if (target.isActive() && target.mayPlace(original)) moved |= moveItemStackTo(original, i, i + 1, false);
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, original); return copy;
    }
}
