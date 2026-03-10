package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.screen.FlueFireboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

public class FlueFireboxBlockEntity extends BaseContainerBlockEntity implements MenuProvider, net.minecraft.world.WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    private int burnTime = 0;
    private int burnTimeTotal = 0;

    private static final int[] SLOTS_FOR_ALL_SIDES = new int[]{0};

    @Override
    public int[] getSlotsForFace(net.minecraft.core.Direction side) {
        return SLOTS_FOR_ALL_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, net.minecraft.world.item.ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
        return slot == 0
                && net.minecraftforge.common.ForgeHooks.getBurnTime(stack, net.minecraft.world.item.crafting.RecipeType.SMELTING) > 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, net.minecraft.world.item.ItemStack stack, net.minecraft.core.Direction side) {
        return false;
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> burnTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public FlueFireboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUE_FIREBOX.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FlueFireboxBlockEntity be) {
        boolean wasLit = be.isLit();

        if (be.burnTime > 0) {
            be.burnTime--;
        }

        ItemStack fuelStack = be.items.get(0);

        if (be.burnTime <= 0 && !fuelStack.isEmpty()) {
            int fuel = ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
            if (fuel > 0) {
                be.burnTime = fuel;
                be.burnTimeTotal = fuel;

                fuelStack.shrink(1);
                if (fuelStack.isEmpty()) {
                    be.items.set(0, ItemStack.EMPTY);
                }
            }
        }

        boolean litNow = be.isLit();
        if (wasLit != litNow) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, litNow), 3);
        }

        if (wasLit != litNow || level.getGameTime() % 20 == 0) {
            be.setChanged();
        }
    }

    public boolean isLit() {
        return burnTime > 0;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.tobacconistmod.flue_firebox");
    }

    @Override
    protected AbstractContainerMenu createMenu(int windowId, Inventory playerInventory) {
        return new FlueFireboxMenu(windowId, playerInventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
    }
}