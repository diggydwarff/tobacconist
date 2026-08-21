package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.util.HookahFuelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.neoforged.neoforge.items.SlotItemHandler;

public class HookahMenu extends AbstractContainerMenu {
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

    public HookahMenu(int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
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
            this.addSlot(new SlotItemHandler(handler, 0, 53, 36) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!HookahFuelHelper.isFuel(stack)) {
                        return false;
                    }

                    ItemStack current = getItem();

                    // If already burning and fuel slot has something in it,
                    // only allow stacking the same fuel type.
                    if (data.get(2) > 0 && !current.isEmpty()) {
                        return ItemStack.isSameItemSameComponents(current, stack);
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
            this.addSlot(new SlotItemHandler(handler, 1, 86, 15));

            // Water slot
            this.addSlot(new SlotItemHandler(handler, 2, 86, 60));
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
        int fuelLevel = this.data.get(2);
        int maxFuelLevel = this.data.get(3);
        int pixelHeight = 63;

        if (maxFuelLevel == 0 || fuelLevel == 0) {
            return 0;
        }

        return fuelLevel * pixelHeight / maxFuelLevel;
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
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 144));
        }
    }
}