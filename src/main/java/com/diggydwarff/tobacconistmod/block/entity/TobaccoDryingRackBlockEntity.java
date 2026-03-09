package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TobaccoDryingRackBlockEntity extends BlockEntity implements Nameable {
    public static final int AIR_DRY_TIME = 20 * 60 * 10;
    public static final int FIRE_DRY_TIME = 20 * 60 * 4;
    public static final int SUN_REQUIRED_TICKS = 20 * 60 * 4;
    private static final long SUN_WINDOW_START = 1000L;
    private static final long SUN_WINDOW_END = 9000L;

    private ItemStack storedLeaf = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private int sunExposureTicks = 0;
    private int interruptionCount = 0;
    private boolean wasActivelyDrying = false;

    public TobaccoDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOBACCO_DRYING_RACK.get(), pos, state);
    }

    @Override
    public Component getName() {
        return Component.literal("Tobacco Drying Rack");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    public boolean hasLeaves() {
        return !storedLeaf.isEmpty();
    }

    public boolean canAccept(ItemStack stack) {
        return storedLeaf.isEmpty() && TobaccoCuringHelper.isRawTobaccoLeaf(stack);
    }

    public ItemStack addLeaves(ItemStack stack) {
        if (!canAccept(stack)) {
            return stack;
        }

        storedLeaf = stack.copy();
        storedLeaf.setCount(1);
        dryingProgress = 0;
        sunExposureTicks = 0;
        interruptionCount = 0;
        wasActivelyDrying = false;
        setChangedAndSync();
        return ItemStack.EMPTY;
    }

    public ItemStack removeLeaves() {
        if (storedLeaf.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = storedLeaf.copy();
        storedLeaf = ItemStack.EMPTY;
        dryingProgress = 0;
        sunExposureTicks = 0;
        interruptionCount = 0;
        wasActivelyDrying = false;
        setChangedAndSync();
        return removed;
    }

    public ItemStack getStoredLeaf() {
        return storedLeaf;
    }

    public int getDryingProgress() {
        return dryingProgress;
    }

    public int getSunExposureTicks() {
        return sunExposureTicks;
    }

    public int getInterruptionCount() {
        return interruptionCount;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level == null || level.isClientSide) {
            return;
        }

        if (storedLeaf.isEmpty()) {
            if (wasActivelyDrying) {
                wasActivelyDrying = false;
                setChanged();
            }
            return;
        }

        boolean overCampfire = state.getValue(TobaccoDryingRackBlock.OVER_CAMPFIRE);
        boolean canDry = overCampfire || canDryInOpenAir(level, pos);

        if (!canDry) {
            if (wasActivelyDrying) {
                interruptionCount++;
                wasActivelyDrying = false;
                setChangedAndSync();
            }
            return;
        }

        wasActivelyDrying = true;
        dryingProgress++;

        if (!overCampfire && hasValidSunExposure(level, pos)) {
            sunExposureTicks++;
        }

        int required = overCampfire ? FIRE_DRY_TIME : AIR_DRY_TIME;
        if (dryingProgress >= required) {
            finishCuring(overCampfire);
        }

        setChanged();
    }

    private void finishCuring(boolean overCampfire) {
        String cureType = overCampfire
                ? TobaccoCuringHelper.CURE_FIRE
                : (sunExposureTicks >= SUN_REQUIRED_TICKS ? TobaccoCuringHelper.CURE_SUN : TobaccoCuringHelper.CURE_AIR);

        ItemStack curedLeaf = TobaccoCuringHelper.getCuredLeafForRaw(storedLeaf);
        if (curedLeaf.isEmpty()) {
            return;
        }

        int quality = TobaccoCuringHelper.buildFinalQuality(storedLeaf, cureType, interruptionCount);
        TobaccoCuringHelper.applyCureData(curedLeaf, cureType, quality);

        storedLeaf = curedLeaf;
        dryingProgress = 0;
        sunExposureTicks = 0;
        interruptionCount = 0;
        wasActivelyDrying = false;
        setChangedAndSync();
    }

    private boolean canDryInOpenAir(Level level, BlockPos pos) {
        return level.canSeeSky(pos.above()) && !level.isRainingAt(pos.above());
    }

    private boolean hasValidSunExposure(Level level, BlockPos pos) {
        if (!level.canSeeSky(pos.above())) {
            return false;
        }
        if (level.isRaining() || level.isThundering() || level.isRainingAt(pos.above())) {
            return false;
        }
        if (!level.isDay()) {
            return false;
        }

        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= SUN_WINDOW_START && dayTime <= SUN_WINDOW_END;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedLeaf.isEmpty()) {
            tag.put("StoredLeaf", storedLeaf.save(new CompoundTag()));
        }
        tag.putInt("DryingProgress", dryingProgress);
        tag.putInt("SunExposureTicks", sunExposureTicks);
        tag.putInt("InterruptionCount", interruptionCount);
        tag.putBoolean("WasActivelyDrying", wasActivelyDrying);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedLeaf = tag.contains("StoredLeaf") ? ItemStack.of(tag.getCompound("StoredLeaf")) : ItemStack.EMPTY;
        dryingProgress = tag.getInt("DryingProgress");
        sunExposureTicks = tag.getInt("SunExposureTicks");
        interruptionCount = tag.getInt("InterruptionCount");
        wasActivelyDrying = tag.getBoolean("WasActivelyDrying");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
