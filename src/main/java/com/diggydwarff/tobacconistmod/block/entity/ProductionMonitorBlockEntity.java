package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.custom.ProductionMonitorBlock;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.screen.ProductionMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/** Persistent configuration and throughput state for the Production Monitor. */
public class ProductionMonitorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DEFAULT_TARGET = 64;
    public static final int MIN_TARGET = 1;
    public static final int MAX_TARGET = Integer.MAX_VALUE;
    private static final long RATE_WINDOW_TICKS = 20L * 60L;
    private static final int PULSE_TICKS = 2;
    private static final int PULSE_GAP_TICKS = 2;
    private static final int STACK_UNIT = 64;

    public enum CountMode {
        ITEMS,
        TRANSFERS,
        // Appended after TRANSFERS so existing saved ordinal 1 remains Transfers.
        STACKS;

        public static CountMode byOrdinal(int ordinal) {
            CountMode[] values = values();
            return values[Math.max(0, Math.min(values.length - 1, ordinal))];
        }
    }

    public enum AtTargetMode {
        KEEP_COUNTING,
        STOP_COUNTING,
        RESET_COUNT;

        public static AtTargetMode byOrdinal(int ordinal) {
            AtTargetMode[] values = values();
            return values[Math.max(0, Math.min(values.length - 1, ordinal))];
        }
    }

    public enum OutputMode {
        NONE,
        PULSE,
        HOLD;

        public static OutputMode byOrdinal(int ordinal) {
            OutputMode[] values = values();
            return values[Math.max(0, Math.min(values.length - 1, ordinal))];
        }
    }

    private long count;
    private int target = DEFAULT_TARGET;
    private CountMode countMode = CountMode.ITEMS;
    private AtTargetMode atTargetMode = AtTargetMode.KEEP_COUNTING;
    private OutputMode outputMode = OutputMode.NONE;
    private boolean externalReset;
    private ItemStack filter = ItemStack.EMPTY;

    private boolean targetLatch;
    private boolean targetValid;
    private boolean lastExternalPowered;
    private boolean externalPowerInitialized;
    private int pulseTicks;
    private int pulseGapTicks;
    private int queuedPulses;

    private final Deque<RateSample> rateSamples = new ArrayDeque<>();
    private double clientRollingRate;
    private long lastTelemetrySyncCount = Long.MIN_VALUE;
    private int lastTelemetrySyncRateHundredths = Integer.MIN_VALUE;
    private boolean lastTelemetrySyncTargetValid;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) count;
                case 1 -> (int) (count >>> 32);
                case 2 -> target;
                case 3 -> countMode.ordinal();
                case 4 -> atTargetMode.ordinal();
                case 5 -> outputMode.ordinal();
                case 6 -> externalReset ? 1 : 0;
                case 7 -> (int) Math.min(Integer.MAX_VALUE, Math.round(getRollingRate() * 100.0D));
                case 8 -> targetValid ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Server-owned data. Client menus use SimpleContainerData and never call into this BE.
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    public ProductionMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRODUCTION_MONITOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ProductionMonitorBlockEntity monitor) {
        long now = level.getGameTime();
        monitor.trimRateSamples(now);

        boolean powered = level.hasNeighborSignal(pos);
        if (!monitor.externalPowerInitialized) {
            // Loading beside an already-powered line is not a rising edge. Establish the baseline
            // first so chunk reloads/restarts cannot repeatedly reset a continuously powered monitor.
            monitor.lastExternalPowered = powered;
            monitor.externalPowerInitialized = true;
        } else {
            if (monitor.externalReset && powered && !monitor.lastExternalPowered) {
                monitor.resetAccumulatedCount();
            }
            monitor.lastExternalPowered = powered;
        }

        monitor.tickPulseQueue();

        BlockPos monitoredPos = monitor.getMonitoredPos();
        BlockEntity targetEntity = level.getBlockEntity(monitoredPos);
        boolean valid = targetEntity instanceof HopperBlockEntity
                || CreateCompat.isProductionMonitorTarget(level, monitoredPos);
        if (valid != monitor.targetValid) {
            monitor.targetValid = valid;
            monitor.setChanged();
        }

        // Spectacles inspection is client-side. Keep its live count/rate/status current without
        // emitting a block-entity packet for every factory transfer.
        if (now % 20L == 0L) {
            monitor.syncClientTelemetryIfChanged();
        }

        if (CreateCompat.loaded()) {
            CreateCompat.observeProductionMonitor(monitor, monitoredPos);
        }
    }

    /** Called only after a transport implementation has confirmed a real successful transfer. */
    public void recordTransfer(ItemStack movedStack) {
        if (level == null || level.isClientSide || movedStack.isEmpty() || !matchesFilter(movedStack)) {
            return;
        }

        int statisticAmount;
        switch (countMode) {
            // Create threshold-style stack units are a presentation of raw item counts in groups of 64.
            // Keeping raw items internally preserves partial-stack overflow exactly across target crossings.
            case ITEMS, STACKS -> statisticAmount = movedStack.getCount();
            case TRANSFERS -> statisticAmount = 1;
            default -> statisticAmount = movedStack.getCount();
        }
        if (statisticAmount <= 0) return;

        long now = level.getGameTime();
        rateSamples.addLast(new RateSample(now, statisticAmount));
        trimRateSamples(now);

        long oldCount = count;
        int crossings = 0;
        switch (atTargetMode) {
            case KEEP_COUNTING -> {
                count = saturatingAdd(count, statisticAmount);
                if (oldCount < target && count >= target) crossings = 1;
            }
            case STOP_COUNTING -> {
                if (oldCount < target) {
                    count = Math.min((long) target, saturatingAdd(oldCount, statisticAmount));
                    if (count >= target) crossings = 1;
                } else {
                    count = target;
                }
            }
            case RESET_COUNT -> {
                long total = saturatingAdd(oldCount, statisticAmount);
                crossings = (int) Math.min(Integer.MAX_VALUE, total / target);
                count = total % target;
            }
        }

        if (crossings > 0) {
            onTargetCrossed(crossings);
        }
        // Menu data slots synchronize count/rate while open. Avoid a block-entity update packet on every
        // factory transfer; persistent state only needs to be marked dirty here.
        setChanged();
    }

    private boolean matchesFilter(ItemStack candidate) {
        if (filter.isEmpty()) return true;
        if (CreateCompat.loaded() && CreateCompat.isCreateProductionMonitorFilter(filter)) {
            return CreateCompat.matchesProductionMonitorFilter(level, filter, candidate);
        }
        return ItemStack.isSameItemSameTags(filter, candidate);
    }

    private void onTargetCrossed(int crossings) {
        if (atTargetMode != AtTargetMode.RESET_COUNT) {
            targetLatch = true;
        }
        if (outputMode == OutputMode.PULSE) {
            queuedPulses = (int) Math.min(Integer.MAX_VALUE, (long) queuedPulses + crossings);
            if (pulseTicks <= 0 && pulseGapTicks <= 0) startNextPulse();
        } else if (outputMode == OutputMode.HOLD) {
            targetLatch = true;
        }
        notifyRedstoneNeighbors();
    }

    private void tickPulseQueue() {
        boolean wasPowered = pulseTicks > 0;
        if (pulseTicks > 0) {
            pulseTicks--;
            if (pulseTicks == 0) pulseGapTicks = PULSE_GAP_TICKS;
        } else if (pulseGapTicks > 0) {
            pulseGapTicks--;
            if (pulseGapTicks == 0 && queuedPulses > 0) startNextPulse();
        } else if (queuedPulses > 0) {
            startNextPulse();
        }
        boolean powered = pulseTicks > 0;
        if (wasPowered != powered) notifyRedstoneNeighbors();
    }

    private void startNextPulse() {
        if (queuedPulses <= 0) return;
        queuedPulses--;
        pulseTicks = PULSE_TICKS;
    }

    public void resetAccumulatedCount() {
        count = 0L;
        targetLatch = false;
        pulseTicks = 0;
        pulseGapTicks = 0;
        queuedPulses = 0;
        setChanged();
        notifyRedstoneNeighbors();
    }

    /** Apply the same ghost filter used by the GUI from the block's external filter target. */
    public boolean setExternalFilter(ItemStack newFilter) {
        ItemStack normalizedFilter = newFilter == null || newFilter.isEmpty()
                ? ItemStack.EMPTY
                : newFilter.copyWithCount(1);
        if (ItemStack.matches(filter, normalizedFilter)) return false;

        filter = normalizedFilter;
        count = 0L;
        rateSamples.clear();
        targetLatch = false;
        pulseTicks = 0;
        pulseGapTicks = 0;
        queuedPulses = 0;
        setChangedAndSync();
        notifyRedstoneNeighbors();
        return true;
    }

    public void applyConfiguration(int newTarget, int countModeOrdinal, int atTargetOrdinal,
                                   int outputOrdinal, boolean newExternalReset, ItemStack newFilter) {
        CountMode nextCountMode = CountMode.byOrdinal(countModeOrdinal);
        AtTargetMode nextAtTarget = AtTargetMode.byOrdinal(atTargetOrdinal);
        OutputMode nextOutput = OutputMode.byOrdinal(outputOrdinal);
        if (nextAtTarget == AtTargetMode.RESET_COUNT && nextOutput == OutputMode.HOLD) {
            nextOutput = OutputMode.PULSE;
        }

        ItemStack normalizedFilter = newFilter == null || newFilter.isEmpty()
                ? ItemStack.EMPTY
                : newFilter.copyWithCount(1);
        boolean filterChanged = !ItemStack.matches(filter, normalizedFilter);
        boolean modeChanged = countMode != nextCountMode;

        target = Math.max(MIN_TARGET, newTarget);
        countMode = nextCountMode;
        atTargetMode = nextAtTarget;
        outputMode = nextOutput;
        externalReset = newExternalReset;
        filter = normalizedFilter;

        if (filterChanged || modeChanged) {
            count = 0L;
            rateSamples.clear();
            targetLatch = false;
            pulseTicks = 0;
            pulseGapTicks = 0;
            queuedPulses = 0;
        } else if (atTargetMode == AtTargetMode.STOP_COUNTING && count > target) {
            count = target;
        }

        // A target change does not reset count. Hold should still reflect an already-satisfied lot,
        // but reconfiguration never fabricates a Pulse event.
        if (atTargetMode != AtTargetMode.RESET_COUNT && count >= target) {
            targetLatch = true;
        } else if (count < target || atTargetMode == AtTargetMode.RESET_COUNT) {
            targetLatch = false;
        }

        setChangedAndSync();
        notifyRedstoneNeighbors();
    }

    private void trimRateSamples(long now) {
        long cutoff = now - RATE_WINDOW_TICKS;
        while (!rateSamples.isEmpty() && rateSamples.peekFirst().tick() < cutoff) {
            rateSamples.removeFirst();
        }
    }

    public double getRollingRate() {
        if (level != null && level.isClientSide) return clientRollingRate;
        if (level == null || rateSamples.isEmpty()) return 0.0D;
        long now = level.getGameTime();
        trimRateSamples(now);
        if (rateSamples.isEmpty()) return 0.0D;

        long total = 0L;
        for (RateSample sample : rateSamples) total += sample.amount();
        // The window itself is one minute, so its running sum is already the per-minute rate.
        // Stack mode stores raw moved-item samples and converts the rolling sum to 64-item equivalents,
        // allowing a useful fractional stacks/min rate without losing partial transfers.
        return countMode == CountMode.STACKS ? total / (double) STACK_UNIT : total;
    }

    public BlockPos getMonitoredPos() {
        Direction facing = getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        return worldPosition.relative(facing);
    }

    public int getRedstoneSignal() {
        return switch (outputMode) {
            case NONE -> 0;
            case PULSE -> pulseTicks > 0 ? 15 : 0;
            case HOLD -> targetLatch ? 15 : 0;
        };
    }

    public int getComparatorSignal() {
        if (target <= 0 || count <= 0) return 0;
        if (count >= target) return 15;
        return Math.max(1, (int) Math.floor((count * 15.0D) / target));
    }

    public long getCount() { return count; }
    public int getTarget() { return target; }
    public CountMode getCountMode() { return countMode; }
    public AtTargetMode getAtTargetMode() { return atTargetMode; }
    public OutputMode getOutputMode() { return outputMode; }
    public boolean isExternalResetEnabled() { return externalReset; }
    public boolean isTargetValid() { return targetValid; }
    public ItemStack getFilter() { return filter.copy(); }
    public ContainerData getMenuData() { return menuData; }

    private static long saturatingAdd(long value, long add) {
        if (add > 0L && value > Long.MAX_VALUE - add) return Long.MAX_VALUE;
        return value + add;
    }

    private void syncClientTelemetryIfChanged() {
        if (level == null || level.isClientSide) return;
        int rateHundredths = (int) Math.min(Integer.MAX_VALUE, Math.round(getRollingRate() * 100.0D));
        if (count == lastTelemetrySyncCount
                && rateHundredths == lastTelemetrySyncRateHundredths
                && targetValid == lastTelemetrySyncTargetValid) {
            return;
        }
        lastTelemetrySyncCount = count;
        lastTelemetrySyncRateHundredths = rateHundredths;
        lastTelemetrySyncTargetValid = targetValid;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void notifyRedstoneNeighbors() {
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Count", count);
        tag.putInt("Target", target);
        tag.putInt("CountMode", countMode.ordinal());
        tag.putInt("AtTarget", atTargetMode.ordinal());
        tag.putInt("Output", outputMode.ordinal());
        tag.putBoolean("ExternalReset", externalReset);
        tag.putBoolean("TargetLatch", targetLatch);
        if (!filter.isEmpty()) tag.put("Filter", filter.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        count = Math.max(0L, tag.getLong("Count"));
        target = Math.max(MIN_TARGET, tag.contains("Target") ? tag.getInt("Target") : DEFAULT_TARGET);
        countMode = CountMode.byOrdinal(tag.getInt("CountMode"));
        atTargetMode = AtTargetMode.byOrdinal(tag.getInt("AtTarget"));
        outputMode = OutputMode.byOrdinal(tag.getInt("Output"));
        if (atTargetMode == AtTargetMode.RESET_COUNT && outputMode == OutputMode.HOLD) outputMode = OutputMode.PULSE;
        externalReset = tag.getBoolean("ExternalReset");
        targetLatch = tag.getBoolean("TargetLatch");
        filter = tag.contains("Filter") ? ItemStack.of(tag.getCompound("Filter")) : ItemStack.EMPTY;
        if (tag.contains("ClientRollingRate")) clientRollingRate = tag.getDouble("ClientRollingRate");
        if (tag.contains("TargetValid")) targetValid = tag.getBoolean("TargetValid");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        tag.putDouble("ClientRollingRate", getRollingRate());
        tag.putBoolean("TargetValid", targetValid);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tobacconistmod.production_monitor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ProductionMonitorMenu(containerId, playerInventory, this, menuData, filter.copy());
    }

    private record RateSample(long tick, int amount) {}
}
