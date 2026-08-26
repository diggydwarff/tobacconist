package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.util.HookahFuelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.SlotItemHandler;

public class HookahMenu extends AbstractContainerMenu {
    public static final int GUI_WIDTH = 200;
    public static final int GUI_HEIGHT = 215;

    public static final int FUEL_SLOT_X = 20;
    public static final int FUEL_SLOT_Y = 52;
    public static final int SHISHA_SLOT_X = 74;
    public static final int SHISHA_SLOT_Y = 31;
    public static final int WATER_SLOT_X = 74;
    public static final int WATER_SLOT_Y = 73;

    public static final int PLAYER_INVENTORY_X = 20;
    public static final int PLAYER_INVENTORY_Y = 125;
    public static final int PLAYER_HOTBAR_Y = 183;

    public final HookahEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 3;

    public static final int FUEL_MENU_SLOT_INDEX = TE_INVENTORY_FIRST_SLOT_INDEX;
    public static final int SHISHA_MENU_SLOT_INDEX = TE_INVENTORY_FIRST_SLOT_INDEX + 1;
    public static final int WATER_MENU_SLOT_INDEX = TE_INVENTORY_FIRST_SLOT_INDEX + 2;

    public HookahMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public HookahMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.HOOKAH_MENU.get(), id);
        checkContainerSize(inv, 3);

        this.blockEntity = (HookahEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        var handler = this.blockEntity.getItemHandler();
        {
            // Fuel slot
            this.addSlot(new SlotItemHandler(handler, 0, FUEL_SLOT_X, FUEL_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!HookahFuelHelper.isFuel(stack)) {
                        return false;
                    }

                    ItemStack current = getItem();

                    // If already burning and fuel slot has something in it,
                    // only allow stacking the same fuel type.
                    if (data.get(2) > 0 && !current.isEmpty()) {
                        return ItemStack.isSameItemSameTags(current, stack);
                    }

                    return true;
                }

                @Override
                public boolean mayPickup(Player player) {
                    // Important:
                    // The burning fuel is already consumed into fuelTime by HookahEntity.tick().
                    // So whatever remains in slot 0 is spare fuel and should be removable.
                    return true;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 64;
                }
            });

            // Shisha slot
            this.addSlot(new SlotItemHandler(handler, 1, SHISHA_SLOT_X, SHISHA_SLOT_Y));

            // Water slot
            this.addSlot(new SlotItemHandler(handler, 2, WATER_SLOT_X, WATER_SLOT_Y));
        }

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 26;
        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledFuelLevel() {
        return getFuelProgressScaled(63);
    }

    public boolean isBurning() {
        return this.data.get(2) > 0;
    }

    public int getFuelProgressScaled(int pixels) {
        int fuelTime = this.data.get(2);
        int maxFuelTime = this.data.get(3);

        if (maxFuelTime <= 0 || fuelTime <= 0) {
            return 0;
        }

        return Math.max(1, fuelTime * pixels / maxFuelTime);
    }

    public int getFuelPercent() {
        int maxFuelTime = this.data.get(3);
        if (maxFuelTime <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, this.data.get(2) * 100 / maxFuelTime));
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Player inventory -> TE
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // Try fuel first
            if (HookahFuelHelper.isFuel(sourceStack)) {
                if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Otherwise fall back to TE slots
                if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX + 1,
                        TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        // TE -> player inventory
        else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    public int getCookProgressScaled(int pixels) {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);

        if (maxProgress <= 0 || progress <= 0) {
            return 0;
        }

        return progress * pixels / maxProgress;
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || blockEntity == null) return false;

        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof HookahBlock) &&
                !(state.getBlock() instanceof DoubleHookahBlock)) {
            return false;
        }

        return player.distanceToSqr(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        ) <= 64.0D;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INVENTORY_X + col * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INVENTORY_X + col * 18, PLAYER_HOTBAR_Y));
        }
    }
}