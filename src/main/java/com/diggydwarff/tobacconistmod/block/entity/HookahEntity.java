package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.custom.DoubleHookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.HookahBlock;
import com.diggydwarff.tobacconistmod.block.custom.NetheriteHookahBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.screen.HookahMenu;
import com.diggydwarff.tobacconistmod.util.HookahFuelHelper;
import com.diggydwarff.tobacconistmod.util.SmokeParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class HookahEntity extends BlockEntity implements MenuProvider {

    private boolean suppressDrops;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> HookahFuelHelper.isFuel(stack);
                case 1 -> stack.is(ModItems.SHISHA_TOBACCO.get());
                case 2 -> isWaterPotion(stack) || stack.is(ModItems.DIRTY_HOOKAH_WATER.get());
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot == 2) {
                ItemStack water = getStackInSlot(2);
                if (water.isEmpty() || isWaterPotion(water)) {
                    waterUses = 0;
                    waterUseThreshold = 0;
                }
            }
            setChanged();
        }
    };

    protected final ContainerData data;
    public int progress = 0;
    private int maxProgress = 6500;
    private int fuelTime = 0;
    private int currentFuelMaxTime = 0;
    private int waterUses = 0;
    private int waterUseThreshold = 0;

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
        return Component.translatable("container.tobacconistmod.hookah");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        HookahEntity master = getMasterEntity();
        return new HookahMenu(id, inventory, master, master.data);
    }

    /**
     * Tall Hookahs expose a block entity on both occupied levels so normal NeoForge/Create
     * discovery works from either half. The upper entity is only a proxy; all inventory and
     * telemetry resolve to the lower master entity.
     */
    public HookahEntity getMasterEntity() {
        if (level == null) return this;
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof DoubleHookahBlock)
                || !state.hasProperty(DoubleHookahBlock.HALF)
                || state.getValue(DoubleHookahBlock.HALF) != DoubleBlockHalf.UPPER) {
            return this;
        }
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        return below instanceof HookahEntity hookah ? hookah : this;
    }

    public IItemHandler getItemHandler() {
        HookahEntity master = getMasterEntity();
        return master == this ? itemHandler : master.itemHandler;
    }

    public int getFuelTime() {
        HookahEntity master = getMasterEntity();
        return master == this ? fuelTime : master.fuelTime;
    }

    public int getCurrentFuelMaxTime() {
        HookahEntity master = getMasterEntity();
        return master == this ? currentFuelMaxTime : master.currentFuelMaxTime;
    }

    public int getFuelRemainingPercent() {
        int total = getCurrentFuelMaxTime();
        return total <= 0 ? 0 : Math.min(100, Math.max(0, getFuelTime() * 100 / total));
    }

    public int getShishaRemainingPercent() {
        ItemStack shisha = getItemHandler().getStackInSlot(1);
        if (shisha.isEmpty()) return 0;
        int max = shisha.getMaxDamage();
        if (max <= 0) return 100;
        return Math.min(100, Math.max(0, (max - shisha.getDamageValue()) * 100 / max));
    }

    /** Preserve inventory/timers while copper Hookahs change oxidation or wax state. */
    public CompoundTag saveTransferData() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    public void loadTransferData(CompoundTag tag) {
        load(tag);
        setChanged();
    }

    public ItemStack getShishaForSmoking() {
        ItemStack shisha = getItemHandler().getStackInSlot(1);
        return shisha.isEmpty() ? ItemStack.EMPTY : shisha.copy();
    }

    public boolean isUsingDirtyWater() {
        return getItemHandler().getStackInSlot(2).is(ModItems.DIRTY_HOOKAH_WATER.get());
    }

    /** Applies the Nausea penalty for drawing through Dirty Hookah Water. */
    public void applyDirtyWaterPenalty(Player player) {
        if (!isUsingDirtyWater()) return;
        player.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION,
                120,
                0,
                false,
                false,
                true
        ));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("hookah.progress", this.progress);
        nbt.putInt("hookah.fuelTime", this.fuelTime);
        nbt.putInt("hookah.currentFuelMaxTime", this.currentFuelMaxTime);
        nbt.putInt("hookah.waterUses", this.waterUses);
        nbt.putInt("hookah.waterUseThreshold", this.waterUseThreshold);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.progress = nbt.getInt("hookah.progress");
        this.fuelTime = nbt.getInt("hookah.fuelTime");
        this.currentFuelMaxTime = nbt.getInt("hookah.currentFuelMaxTime");
        this.waterUses = nbt.getInt("hookah.waterUses");
        this.waterUseThreshold = nbt.getInt("hookah.waterUseThreshold");
    }

    public void drops() {
        // Creative removal suppresses inventory drops.
        if (suppressDrops) {
            return;
        }

        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    /** Clears the Hookah inventory without producing drops during a Creative break. */
    public void clearContentsForCreativeBreak() {
        suppressDrops = true;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HookahEntity pEntity) {
        if (level.isClientSide()) {
            // Netherite Hookahs emit a regular client-side portal ambience.
            if (state.getBlock() instanceof NetheriteHookahBlock && level.getGameTime() % 7L == 0L) {
                int count = 2 + level.random.nextInt(2);
                for (int i = 0; i < count; i++) {
                    double x = pos.getX() + 0.25D + level.random.nextDouble() * 0.50D;
                    double y = pos.getY() + 0.18D + level.random.nextDouble() * 1.22D;
                    double z = pos.getZ() + 0.25D + level.random.nextDouble() * 0.50D;
                    double vx = (level.random.nextDouble() - 0.5D) * 0.018D;
                    double vy = 0.014D + level.random.nextDouble() * 0.018D;
                    double vz = (level.random.nextDouble() - 0.5D) * 0.018D;
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, vx, vy, vz);
                }
                if (level.random.nextInt(3) == 0) {
                    level.addParticle(ParticleTypes.PORTAL,
                            pos.getX() + 0.5D, pos.getY() + 0.55D + level.random.nextDouble() * 0.65D, pos.getZ() + 0.5D,
                            (level.random.nextDouble() - 0.5D) * 0.10D,
                            (level.random.nextDouble() - 0.5D) * 0.04D,
                            (level.random.nextDouble() - 0.5D) * 0.10D);
                }
            }
            return;
        }

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
            BlockState blockState = level.getBlockState(pos);
            double smokeY = pos.getY() + 0.90D;
            if (blockState.getBlock() instanceof DoubleHookahBlock) {
                // The tall/material models are about 1.5 blocks high while still reserving
                // the upper block for placement. Emit from the actual bowl instead of
                // floating near the top of the reserved second block.
                smokeY = pos.getY() + 1.56D;
            }

            // Emit the active plume every 10 ticks.
            if (level.getGameTime() % 10L == 0L) {
                SmokeParticleHelper.spawnServerHookahSmoke(
                        serverLevel,
                        pos.getX() + 0.5D,
                        smokeY,
                        pos.getZ() + 0.5D
                );
            }

            pEntity.progress++;
            pEntity.fuelTime--;

            // Shisha durability is consumed while the Hookah is active.
            ItemStack shisha = pEntity.itemHandler.getStackInSlot(1).copy();
            shisha.setDamageValue(shisha.getDamageValue() + 1);

            if (shisha.getDamageValue() >= shisha.getMaxDamage()) {
                pEntity.itemHandler.extractItem(1, 1, false);
                pEntity.recordCompletedShishaUse(level);
            } else {
                pEntity.itemHandler.setStackInSlot(1, shisha);
            }

            setChanged(level, pos, state);

            if (pEntity.progress >= pEntity.maxProgress) {
                // Shisha durability is the consumption timer; there is no output slot.
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

    private void recordCompletedShishaUse(Level level) {
        ItemStack water = itemHandler.getStackInSlot(2);
        if (!isWaterPotion(water)) return;

        if (waterUseThreshold < 2 || waterUseThreshold > 5) {
            waterUseThreshold = 2 + level.random.nextInt(4); // 2-5 completed Shisha loads
        }

        waterUses++;
        if (waterUses >= waterUseThreshold) {
            itemHandler.setStackInSlot(2, new ItemStack(ModItems.DIRTY_HOOKAH_WATER.get()));
            waterUses = 0;
            waterUseThreshold = 0;
        }
        setChanged();
    }

    private static boolean isWaterPotion(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    private static boolean canProcess(HookahEntity entity) {
        boolean hasShishaInSlot = entity.itemHandler.getStackInSlot(1).is(ModItems.SHISHA_TOBACCO.get());
        ItemStack water = entity.itemHandler.getStackInSlot(2);
        boolean hasUsableWater = isWaterPotion(water) || water.is(ModItems.DIRTY_HOOKAH_WATER.get());
        return hasShishaInSlot && hasUsableWater;
    }
}