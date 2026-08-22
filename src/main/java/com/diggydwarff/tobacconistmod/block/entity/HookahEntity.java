package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.screen.HookahMenu;
import com.diggydwarff.tobacconistmod.util.HookahFuelHelper;
import com.diggydwarff.tobacconistmod.util.SmokeParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class HookahEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> HookahFuelHelper.isFuel(stack);
                case 1 -> stack.is(ModItems.SHISHA_TOBACCO.get());
                case 2 -> isWaterPotion(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

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

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.put("inventory", itemHandler.serializeNBT(registries));
        nbt.putInt("hookah.progress", this.progress);
        nbt.putInt("hookah.fuelTime", this.fuelTime);
        nbt.putInt("hookah.currentFuelMaxTime", this.currentFuelMaxTime);
        super.saveAdditional(nbt, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        itemHandler.deserializeNBT(registries, nbt.getCompound("inventory"));
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
            BlockPos smokePos = pos;
            BlockState blockState = level.getBlockState(pos);

            if (blockState.getBlock() instanceof DoubleHookahBlock) {
                smokePos = pos.above(); // move to top block
            }

            SmokeParticleHelper.spawnServerHookahSmoke(
                    serverLevel,
                    smokePos.getX() + 0.5D,
                    smokePos.getY() + 0.9D,
                    smokePos.getZ() + 0.5D
            );

            pEntity.progress++;
            pEntity.fuelTime--;

            // keep old shisha durability damage logic
            ItemStack shisha = pEntity.itemHandler.getStackInSlot(1).copy();
            shisha.setDamageValue(shisha.getDamageValue() + 1);

            if (shisha.getDamageValue() >= shisha.getMaxDamage()) {
                pEntity.itemHandler.extractItem(1, 1, false);
            } else {
                pEntity.itemHandler.setStackInSlot(1, shisha);
            }

            setChanged(level, pos, state);

            if (pEntity.progress >= pEntity.maxProgress) {
                // The shisha durability is the actual consumption timer. At this point
                // the spent shisha stack has already been removed above; there is no
                // crafted output to move into another slot.
                pEntity.resetProgress();
            }
        } else {
            pEntity.resetProgress();
            setChanged(level, pos, state);
        }

        if (state.hasProperty(BlockStateProperties.LIT)) {
            boolean current = state.getValue(BlockStateProperties.LIT);
            if (current != litNow) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, litNow), 3);
            }
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static boolean isWaterPotion(ItemStack stack) {
        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return stack.is(Items.POTION) && potionContents.is(Potions.WATER);
    }

    private static boolean canProcess(HookahEntity entity) {
        boolean hasShishaInSlot = entity.itemHandler.getStackInSlot(1).is(ModItems.SHISHA_TOBACCO.get());
        boolean hasWaterInSlot = isWaterPotion(entity.itemHandler.getStackInSlot(2));
        return hasShishaInSlot && hasWaterInSlot;
    }
}