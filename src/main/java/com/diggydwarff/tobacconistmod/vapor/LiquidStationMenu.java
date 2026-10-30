package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class LiquidStationMenu extends AbstractContainerMenu {
    public static final int WIDTH = 232;
    public static final int BLEND_PANEL_HEIGHT = 158, EXTRACTION_PANEL_HEIGHT = 112;
    public static final int[][] BLEND_SLOTS = {{14,36},{90,36},{166,36},{14,69},{90,69},{166,69},{87,136},{137,136},{14,136},{61,76},{190,136}};
    public static final int[][] EXTRACTION_SLOTS = {{14,40},{61,40},{108,40},{14,84},{90,84},{166,84},{87,158},{190,40},{14,158},{61,76},{190,76}};
    private final Inventory playerInventory;
    private final BlockPos pos;
    private final boolean blending;
    private final ContainerData data;
    private final ItemStackHandler stationInventory;

    public LiquidStationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), buffer.readBoolean(), new ItemStackHandler(11), new SimpleContainerData(13));
    }
    public LiquidStationMenu(int id, Inventory inventory, LiquidStationEntity station) {
        this(id, inventory, station.getBlockPos(), station.isBlending(), station.inventory, station.data);
    }
    private LiquidStationMenu(int id, Inventory inventory, BlockPos pos, boolean blending, ItemStackHandler items, ContainerData data) {
        super(VaporContent.STATION_MENU.get(), id); this.playerInventory = inventory;
        this.pos = pos; this.blending = blending; this.data = data; this.stationInventory = items;
        for (int slot = 0; slot < 11; slot++) {
            int[][] layout = blending ? BLEND_SLOTS : EXTRACTION_SLOTS;
            int x = layout[slot][0], y = layout[slot][1];
            final int index = slot;
            addSlot(new SlotItemHandler(items, slot, x, y) {
                @Override public boolean isActive() { return blending ? index != 9 : index < 3 || index == 7 || index == 9 || index == 10; }
                @Override public boolean mayPlace(ItemStack stack) {
                    return index != 7 && index != 10 && super.mayPlace(stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, 9 + row * 9 + col, 36 + col * 18, inventoryY() + 18 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 36 + col * 18, inventoryY() + 76));
        addDataSlots(data);
    }
    public boolean blending() { return blending; }
    public int inventoryY() { return (blending ? BLEND_PANEL_HEIGHT : EXTRACTION_PANEL_HEIGHT) + 2; }
    public int value(int index) { return data.get(index); }
    public BlockPos pos() { return pos; }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id > 7 || !player.isAlive() || player.isSpectator() || !stillValid(player)) return false;
        if (player.level.getBlockEntity(pos) instanceof LiquidStationEntity station) {
            station.button(id);
            if (id == 4 && player instanceof net.minecraft.server.level.ServerPlayer server) {
                VaporAdvancements.onRecipeSaved(server, station.inventory.getStackInSlot(LiquidStationEntity.CARD));
            }
            return true;
        }
        return false;
    }
    @Override public boolean stillValid(Player player) {
        return player.level.getBlockEntity(pos) instanceof LiquidStationEntity station
                && station.inventory == stationInventory && station.isBlending() == blending
                && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64;
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        ItemStack previousCard = stationInventory.getStackInSlot(LiquidStationEntity.CARD);
        super.clicked(slot, button, type, player);
        pullInsertedRecipeEssences(player, previousCard);
    }
    private void pullInsertedRecipeEssences(Player player, ItemStack previousCard) {
        if (player.level.isClientSide || player.getInventory() != playerInventory
                || !blending || !player.isAlive() || player.isSpectator()
                || !stillValid(player) || stationInventory.getStackInSlot(LiquidStationEntity.CARD) == previousCard) return;
        if (player.level.getBlockEntity(pos) instanceof LiquidStationEntity station) {
            station.pullRecipeEssences(playerInventory);
        }
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack previousCard = stationInventory.getStackInSlot(LiquidStationEntity.CARD);
        ItemStack original = slot.getItem(), copy = original.copy();
        if (index < 11) {
            if (!moveItemStackTo(original, 11, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < 11 && !original.isEmpty(); i++) {
                Slot target = slots.get(i);
                if (blending && i >= 3 && i < 6
                        && player.level.getBlockEntity(pos) instanceof LiquidStationEntity station
                        && !station.canLoadBottle(i, original)) continue;
                if (target.isActive() && target.mayPlace(original)) moved |= moveItemStackTo(original, i, i + 1, false);
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, original);
        pullInsertedRecipeEssences(player, previousCard);
        return copy;
    }
}
