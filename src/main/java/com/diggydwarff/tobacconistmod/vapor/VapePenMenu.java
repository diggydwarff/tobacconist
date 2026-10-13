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
        addSlot(new Slot(inputs, 0, 26, 44) { @Override public boolean mayPlace(ItemStack stack) { return VaporData.isVapeRefill(stack) && VaporData.blend(stack) != null && VaporData.blend(stack).usable(); } });
        addSlot(new Slot(inputs, 1, 134, 44) { @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.REDSTONE); } });
        addInventorySlot(deviceSlot, 80, 44);
        int inventoryY = isBoxMod() ? 164 : 132;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addInventorySlot(9 + row * 9 + col, 8 + col * 18, inventoryY + row * 18);
        for (int col = 0; col < 9; col++) addInventorySlot(col, 8 + col * 18, inventoryY + 58);
    }
    private void addInventorySlot(int inventoryIndex, int x, int y) {
        addSlot(new Slot(inventory, inventoryIndex, x, y) {
            // Slot.index is the menu index; lock by the player's inventory index instead.
            @Override public boolean mayPickup(Player player) { return inventoryIndex != deviceSlot; }
            @Override public boolean mayPlace(ItemStack stack) { return inventoryIndex != deviceSlot; }
        });
    }
    public ItemStack pen() { return inventory.getItem(deviceSlot); }
    public boolean isBoxMod() { return VapePenItem.hasPowerControl(device); }
    public boolean canRefill() {
        ItemStack bottle = inputs.getItem(0);
        ItemStack current = pen();
        LiquidBlend blend = VaporData.blend(bottle);
        return VaporData.isVapeRefill(bottle) && blend != null && blend.usable()
                && VaporData.volume(bottle) > 0 && VapePenItem.volume(current) < VapePenItem.capacity(current)
                && (VapePenItem.volume(current) == 0 || blend.equals(VaporData.blend(current)));
    }
    public boolean canCharge() { return inputs.getItem(1).is(Items.REDSTONE) && VapePenItem.charge(pen()) < VapePenItem.maxCharge(pen()); }
    @Override public boolean stillValid(Player player) { return VapePenItem.isDevice(device) && inventory.getItem(deviceSlot) == device; }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (type == ClickType.SWAP && (button == deviceSlot || button == 40 && deviceSlot == 40)) return;
        super.clicked(slot, button, type, player);
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (!player.isAlive() || player.isSpectator() || !stillValid(player)) return false;
        if (id == 0) {
            ItemStack bottle = inputs.getItem(0);
            if (!canRefill()) return false;
            LiquidBlend blend = VaporData.blend(bottle);
            int amount = Math.min(VapePenItem.capacity(device) - VapePenItem.volume(device), VaporData.volume(bottle));
            if (amount <= 0) return false;
            device.set(VaporContent.BLEND.get(), blend); device.set(VaporContent.VOLUME.get(), VapePenItem.volume(device) + amount);
            int remaining = VaporData.volume(bottle) - amount;
            ItemStack returned = remaining == 0 ? VaporData.emptyGlass(bottle) : bottle.copyWithCount(1);
            if (remaining > 0) VaporData.setBottleVolume(returned, remaining);
            if (bottle.getCount() == 1) inputs.setItem(0, returned);
            else {
                bottle.shrink(1);
                if (!player.addItem(returned)) player.drop(returned, false);
            }
        } else if (id == 1) {
            if (!canCharge()) return false;
            inputs.removeItem(1, 1); device.set(VaporContent.CHARGE.get(), Math.min(VapePenItem.maxCharge(device), VapePenItem.charge(device) + 20));
        } else if ((id == 2 || id == 3) && isBoxMod()) {
            int step = id == 2 ? 10 : -10;
            device.set(VaporContent.WATTAGE.get(), Math.clamp(VapePenItem.wattage(device) + step, 10, 40));
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
