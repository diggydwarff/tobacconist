package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.block.entity.FlueFireboxBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

public class FlueFireboxMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public FlueFireboxMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, getBlockEntityContainer(playerInventory, buf), new SimpleContainerData(2));
    }

    public FlueFireboxMenu(int windowId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.FLUE_FIREBOX_MENU.get(), windowId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 2);

        this.container = container;
        this.data = data;

        this.container.startOpen(playerInventory.player);

        // Fuel slot only
        this.addSlot(new Slot(container, 0, 80, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            }
        });

        // Player inventory
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(data);
    }

    private static Container getBlockEntityContainer(Inventory playerInventory, FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof FlueFireboxBlockEntity be) {
            return be;
        }
        return new SimpleContainer(1);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public boolean isLit() {
        return data.get(0) > 0;
    }

    public int getBurnProgress() {
        int burnTime = data.get(0);
        int burnTimeTotal = data.get(1);
        if (burnTimeTotal == 0) {
            burnTimeTotal = 200;
        }
        return burnTime * 13 / burnTimeTotal;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37) {
                    if (!this.moveItemStackTo(stack, 1, 28, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemstack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
}