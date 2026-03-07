package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.screen.HookahMenu;
import com.diggydwarff.tobacconistmod.util.HookahFuelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HookahEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    public int progress = 0;
    private int maxProgress = 5000;
    private int fuelTime = 0;
    private int currentFuelMaxTime = 0;

    public HookahEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOOKAH.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> HookahEntity.this.progress;
                    case 1 -> HookahEntity.this.maxProgress;
                    case 2 -> HookahEntity.this.fuelTime;
                    case 3 -> HookahEntity.this.currentFuelMaxTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> HookahEntity.this.progress = value;
                    case 1 -> HookahEntity.this.maxProgress = value;
                    case 2 -> HookahEntity.this.fuelTime = value;
                    case 3 -> HookahEntity.this.currentFuelMaxTime = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Hookah");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new HookahMenu(id, inventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("hookah.progress", this.progress);
        nbt.putInt("hookah.fuelTime", this.fuelTime);
        nbt.putInt("hookah.currentFuelMaxTime", this.currentFuelMaxTime);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.progress = nbt.getInt("hookah.progress");
        this.fuelTime = nbt.getInt("hookah.fuelTime");
        this.currentFuelMaxTime = nbt.getInt("hookah.currentFuelMaxTime");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HookahEntity pEntity) {
        if (level.isClientSide()) return;

        boolean litNow = false;

        // Furnace-style fuel start:
        // if no active fuel, consume one fuel item now and start burn time
        if (pEntity.fuelTime <= 0) {
            ItemStack fuel = pEntity.itemHandler.getStackInSlot(0);
            float mult = HookahFuelHelper.getMultiplier(fuel);

            if (mult > 0) {
                pEntity.itemHandler.extractItem(0, 1, false);
                pEntity.fuelTime = (int) (5000 * mult);
                pEntity.currentFuelMaxTime = pEntity.fuelTime;
                pEntity.setChanged();
            }
        }

        if (canProcess(pEntity) && pEntity.fuelTime > 0) {
            litNow = true;

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    1, 0, 0, 0, 0
            );

            pEntity.progress++;
            pEntity.fuelTime--;

            // keep old shisha durability damage logic
            ItemStack shisha = pEntity.itemHandler.getStackInSlot(1);
            shisha.setDamageValue(shisha.getDamageValue() + 1);

            if (shisha.getDamageValue() >= shisha.getMaxDamage()) {
                pEntity.itemHandler.extractItem(1, 1, false);
            }

            setChanged(level, pos, state);

            if (pEntity.progress >= pEntity.maxProgress) {
                craftItem(pEntity);
            }
        } else {
            pEntity.resetProgress();
            setChanged(level, pos, state);
        }

        if (state.getBlock() instanceof HookahBlock && state.hasProperty(HookahBlock.LIT)) {
            boolean current = state.getValue(HookahBlock.LIT);
            if (current != litNow) {
                level.setBlock(pos, state.setValue(HookahBlock.LIT, litNow), 3);
            }
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static void craftItem(HookahEntity pEntity) {
        if (canProcess(pEntity)) {
            pEntity.itemHandler.extractItem(1, 1, false);

            pEntity.itemHandler.setStackInSlot(2,
                    new ItemStack(ModItems.SHISHA_TOBACCO.get(),
                            pEntity.itemHandler.getStackInSlot(2).getCount() + 1));

            pEntity.resetProgress();
        }
    }

    private static boolean canProcess(HookahEntity entity) {
        boolean hasShishaInSlot =
                entity.itemHandler.getStackInSlot(1).getItem() == ModItems.SHISHA_TOBACCO.get();

        boolean hasWaterInSlot =
                entity.itemHandler.getStackInSlot(2).is(Items.POTION) &&
                        entity.itemHandler.getStackInSlot(2).getTag() != null &&
                        entity.itemHandler.getStackInSlot(2).getTag().getString("Potion").equals("minecraft:water");

        return hasShishaInSlot && hasWaterInSlot;
    }
}