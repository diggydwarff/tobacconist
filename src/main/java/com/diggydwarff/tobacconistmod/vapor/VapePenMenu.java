package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** Locks the device slot and returns all temporary inputs when the menu closes. */
public final class VapePenMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final int deviceSlot;
    private final ItemStack device;
    private final SimpleContainer inputs = new SimpleContainer(2);
    public VapePenMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) { this(id, inventory, buffer.readVarInt()); }
    public VapePenMenu(int id, Inventory inventory, int deviceSlot) {
        super(VaporContent.PEN_MENU.get(), id); this.inventory = inventory; this.deviceSlot = deviceSlot;
        device = deviceSlot >= 0 && deviceSlot < inventory.getContainerSize() ? inventory.getItem(deviceSlot) : ItemStack.EMPTY;
        addSlot(new Slot(inputs, 0, 44, 46) { @Override public boolean mayPlace(ItemStack stack) { return stack.is(VaporContent.VAPE_LIQUID.get()) && VaporData.blend(stack) != null && VaporData.blend(stack).usable(); } });
        addSlot(new Slot(inputs, 1, 116, 46) { @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.REDSTONE); } });
        addInventorySlot(deviceSlot, 80, 46);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addInventorySlot(9 + row * 9 + col, 8 + col * 18, 112 + row * 18);
        for (int col = 0; col < 9; col++) addInventorySlot(col, 8 + col * 18, 170);
    }
    private void addInventorySlot(int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override public boolean mayPickup(Player player) { return index != deviceSlot; }
            @Override public boolean mayPlace(ItemStack stack) { return index != deviceSlot; }
        });
    }
    public ItemStack pen() { return inventory.getItem(deviceSlot); }
    @Override public boolean stillValid(Player player) { return device.is(VaporContent.VAPE_PEN.get()) && inventory.getItem(deviceSlot) == device; }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (type == ClickType.SWAP && (button == deviceSlot || button == 40 && deviceSlot == 40)) return;
        super.clicked(slot, button, type, player);
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player)) return false;
        if (id == 0) {
            ItemStack bottle = inputs.getItem(0);
            if (!bottle.is(VaporContent.VAPE_LIQUID.get())) return false;
            LiquidBlend blend = VaporData.blend(bottle), old = VaporData.blend(device);
            if (blend == null || !blend.usable() || VapePenItem.volume(device) > 0 && !blend.equals(old)) return false;
            int amount = Math.min(250 - VapePenItem.volume(device), VaporData.volume(bottle));
            if (amount <= 0) return false;
            device.set(VaporContent.BLEND.get(), blend); device.set(VaporContent.VOLUME.get(), VapePenItem.volume(device) + amount);
            int remaining = VaporData.volume(bottle) - amount;
            if (remaining == 0) inputs.setItem(0, new ItemStack(Items.GLASS_BOTTLE)); else bottle.set(VaporContent.VOLUME.get(), remaining);
        } else if (id == 1) {
            if (!inputs.getItem(1).is(Items.REDSTONE) || VaporData.charge(device) >= 100) return false;
            inputs.removeItem(1, 1); device.set(VaporContent.CHARGE.get(), Math.min(100, VaporData.charge(device) + 20));
        } else return false;
        inputs.setChanged(); inventory.setChanged(); broadcastChanges(); return true;
    }
    @Override public void removed(Player player) { super.removed(player); if (!player.level().isClientSide) clearContainer(player, inputs); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index); if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), copy = stack.copy();
        if (index < 3) { if (!moveItemStackTo(stack, 3, slots.size(), true)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(player, stack); return copy;
    }
}
