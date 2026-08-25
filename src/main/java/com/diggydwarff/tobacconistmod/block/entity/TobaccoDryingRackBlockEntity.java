package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public class TobaccoDryingRackBlockEntity extends BlockEntity implements WorldlyContainer {

    public static final int MAX_LEAVES = 16;

    public static final int AIR_DRY_TIME = 72000;
    public static final int SUN_DRY_TIME = 48000;
    public static final int GLASS_SUN_DRY_TIME = 54000;
    public static final int FIRE_DRY_TIME = 24000;
    public static final int FLUE_DRY_TIME = 36000;

    // Create fan assistance accelerates the existing rack timers; it never converts leaves by
    // itself. Plain airflow is a strong air-cure boost, while catalyst-heated air is faster.
    private static final int CREATE_FAN_AIR_TICK_RATE = 4;
    private static final int CREATE_FAN_HEATED_TICK_RATE = 6;
    private static final int CREATE_FAN_ASSIST_REFRESH_TICKS = 10;

    private static final int[] SLOTS_FOR_SIDES = new int[]{0};
    private static final int[] SLOTS_FOR_BOTTOM = new int[]{0};
    private static final int[] NO_SLOTS = new int[]{};

    private ItemStack storedLeaf = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private int sunExposureTicks = 0;
    private int interruptionCount = 0;
    private boolean lastTickHadValidDrying = false;
    private boolean usedFireDrying = false;
    private boolean usedFlueDrying = false;

    private int directRainExposureTicks = 0;
    private int wetDamagePenalty = 0;

    private int airTicks = 0;
    private int sunTicks = 0;
    private int fireTicks = 0;
    private int flueTicks = 0;

    // Runtime-only Create environment cache. There is no need to persist this; the fan resolver
    // re-evaluates the actual airflow after load and whenever the short cache expires.
    private int createFanAssistRefresh = 0;
    private CreateCompat.FanCuringAssist cachedCreateFanAssist = CreateCompat.FanCuringAssist.NONE;

    private final EnumMap<Direction, IItemHandler> sidedItemHandlers = new EnumMap<>(Direction.class);
    private final IItemHandler unsidedItemHandler;

    public TobaccoDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOBACCO_DRYING_RACK.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sidedItemHandlers.put(direction, new DryingRackItemHandler(this, direction));
        }
        unsidedItemHandler = new DryingRackItemHandler(this, null);
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? unsidedItemHandler : sidedItemHandlers.get(side);
    }

    public ItemStack getStoredLeaf() {
        return storedLeaf;
    }

    public boolean hasLeaves() {
        return !storedLeaf.isEmpty() && storedLeaf.getCount() > 0;
    }

    public int getLeafCount() {
        return hasLeaves() ? storedLeaf.getCount() : 0;
    }

    /** Maps the 0..16 inventory count onto the four visual rack models. */
    public int getVisualLoadStage() {
        int count = Math.min(MAX_LEAVES, getLeafCount());
        if (count <= 0) return 0;
        if (count <= 5) return 1;
        if (count <= 11) return 2;
        return 3;
    }

    public boolean isFull() {
        return hasLeaves() && storedLeaf.getCount() >= MAX_LEAVES;
    }

    public boolean canAccept(ItemStack stack) {
        if (stack.isEmpty() || !isValidLeaf(stack)) {
            return false;
        }

        if (isFinished()) {
            return false;
        }

        if (storedLeaf.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(storedLeaf, stack)) {
            return false;
        }

        if (storedLeaf.getCount() >= MAX_LEAVES) {
            return false;
        }

        return !isBatchLocked();
    }

    public boolean addOneLeaf(ItemStack stack) {
        if (!canAccept(stack)) {
            return false;
        }

        if (storedLeaf.isEmpty()) {
            storedLeaf = stack.copyWithCount(1);
            dryingProgress = 0;
            sunExposureTicks = 0;
            interruptionCount = 0;
            usedFireDrying = false;
            usedFlueDrying = false;
            lastTickHadValidDrying = false;
            directRainExposureTicks = 0;
            wetDamagePenalty = 0;
            airTicks = 0;
            sunTicks = 0;
            fireTicks = 0;
            flueTicks = 0;
        } else {
            storedLeaf.grow(1);
        }

        syncRackState();
        syncToClient();
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return storedLeaf.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? storedLeaf : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || amount <= 0 || storedLeaf.isEmpty() || !isFinished()) {
            return ItemStack.EMPTY;
        }

        ItemStack out = storedLeaf.split(amount);
        if (storedLeaf.isEmpty()) {
            storedLeaf = ItemStack.EMPTY;
        }

        setChanged();
        syncRackState();
        syncToClient();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0 || storedLeaf.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack out = storedLeaf;
        storedLeaf = ItemStack.EMPTY;

        setChanged();
        syncRackState();
        syncToClient();
        return out;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;

        storedLeaf = stack.copy();

        if (storedLeaf.getCount() > MAX_LEAVES) {
            storedLeaf.setCount(MAX_LEAVES);
        }

        setChanged();
        syncRackState();
        syncToClient();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_FOR_BOTTOM;
        }

        if (side == Direction.UP) {
            return NO_SLOTS;
        }

        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (slot != 0) return false;
        if (side == Direction.UP) return false;
        if (side == Direction.DOWN) return false;

        if (!isValidLeaf(stack)) return false;
        if (isFinished()) return false;

        if (storedLeaf.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(storedLeaf, stack)) {
            return false;
        }

        return storedLeaf.getCount() < MAX_LEAVES && !isBatchLocked();
    }

    @Override
    public int getMaxStackSize() {
        return MAX_LEAVES;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (slot != 0) return false;
        if (side != Direction.DOWN) return false;

        return isFinished();
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        storedLeaf = ItemStack.EMPTY;
        setChanged();
        syncRackState();
        syncToClient();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClient() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getDryProgressPercent() {
        if (!hasLeaves()) {
            return 0;
        }

        if (isFinished()) {
            return 100;
        }

        int dominantTicks = Math.max(
                Math.max(fireTicks, sunTicks),
                Math.max(airTicks, flueTicks)
        );

        int needed;
        if (dominantTicks == fireTicks) {
            needed = FIRE_DRY_TIME;
        } else if (dominantTicks == flueTicks) {
            needed = FLUE_DRY_TIME;
        } else if (dominantTicks == sunTicks) {
            needed = isCurrentlyGlassSunCuring() ? GLASS_SUN_DRY_TIME : SUN_DRY_TIME;
        } else {
            needed = AIR_DRY_TIME;
        }

        if (needed <= 0) {
            return 0;
        }

        return Math.min(100, (dominantTicks * 100) / needed);
    }

    public int getEstimatedTicksRemaining() {
        if (!hasLeaves() || isFinished()) return 0;
        int pct = getDryProgressPercent();
        int required = getRequiredDryingTime();
        int remainingCureTicks = Math.max(0,
                (int) Math.ceil(required * ((100.0 - pct) / 100.0)));
        int tickRate = getCurrentDryingTickRate();
        return Math.max(0, (int) Math.ceil((double) remainingCureTicks / tickRate));
    }

    public int getVisualCureStage() {
        if (!hasLeaves()) {
            return 0;
        }

        if (isFinished()) {
            return 5;
        }

        int pct = getDryProgressPercent();

        if (pct >= 80) return 4;
        if (pct >= 60) return 3;
        if (pct >= 40) return 2;
        if (pct >= 20) return 1;
        return 0;
    }

    private int getRequiredDryingTime() {
        if (usedFireDrying && fireTicks >= flueTicks && fireTicks >= sunTicks && fireTicks >= airTicks) {
            return FIRE_DRY_TIME;
        }
        if (usedFlueDrying && flueTicks >= fireTicks && flueTicks >= sunTicks && flueTicks >= airTicks) {
            return FLUE_DRY_TIME;
        }
        if (sunTicks >= fireTicks && sunTicks >= flueTicks && sunTicks >= airTicks) {
            return isCurrentlyGlassSunCuring() ? GLASS_SUN_DRY_TIME : SUN_DRY_TIME;
        }
        return AIR_DRY_TIME;
    }

    private int getCurrentDryingTickRate() {
        if (level == null) {
            return 1;
        }

        boolean directRain = level.isRainingAt(worldPosition.above())
                && level.canSeeSky(worldPosition.above());
        if (directRain) {
            return 1;
        }

        CreateCompat.FanCuringAssist fanAssist = getCreateFanAssist();
        if (fanAssist == CreateCompat.FanCuringAssist.FIRE) {
            return CREATE_FAN_HEATED_TICK_RATE;
        }
        if (fanAssist == CreateCompat.FanCuringAssist.FLUE
                && !isOverLitCampfire(level, worldPosition)) {
            return CREATE_FAN_HEATED_TICK_RATE;
        }
        if (fanAssist == CreateCompat.FanCuringAssist.AIR
                && !isOverLitCampfire(level, worldPosition)
                && !canFlueCure(level, worldPosition)) {
            // Plain airflow assists both Air Curing and an otherwise-valid Sun Cure.
            return CREATE_FAN_AIR_TICK_RATE;
        }
        return 1;
    }

    public MutableComponent getRackStatusComponent() {
        if (!hasLeaves()) {
            return Component.translatable("tobacconistmod.ui.empty");
        }
        if (isFinished()) {
            return Component.translatable(
                    "tobacconistmod.status.finished_cure",
                    TobaccoText.cure(TobaccoCuringHelper.getCureType(storedLeaf))
            );
        }
        return Component.translatable(
                "tobacconistmod.status.method_progress",
                getCurrentCureMethodComponent(),
                getDryProgressPercent()
        );
    }

    public MutableComponent getCurrentCureMethodComponent() {
        if (level == null || !hasLeaves()) {
            return Component.translatable("tobacconistmod.ui.empty");
        }
        if (isFinished()) {
            return TobaccoText.cure(TobaccoCuringHelper.getCureType(storedLeaf));
        }

        boolean directRain = level.isRainingAt(worldPosition.above()) && level.canSeeSky(worldPosition.above());
        CreateCompat.FanCuringAssist fanAssist = directRain
                ? CreateCompat.FanCuringAssist.NONE
                : getCreateFanAssist();

        if (isOverLitCampfire(level, worldPosition) || fanAssist == CreateCompat.FanCuringAssist.FIRE) {
            return Component.translatable(fanAssist == CreateCompat.FanCuringAssist.FIRE
                    ? "tobacconistmod.cure_method.fire_create_smoke"
                    : "tobacconistmod.cure_method.fire_campfire_heat");
        }
        if (canFlueCure(level, worldPosition) || fanAssist == CreateCompat.FanCuringAssist.FLUE) {
            return Component.translatable(fanAssist == CreateCompat.FanCuringAssist.FLUE
                    ? "tobacconistmod.cure_method.flue_create_heat"
                    : "tobacconistmod.cure_method.flue_barn_heat");
        }
        if (hasDirectSunlight(level, worldPosition)) {
            if (fanAssist == CreateCompat.FanCuringAssist.AIR) {
                return Component.translatable(isGlassSunCure(level, worldPosition)
                        ? "tobacconistmod.cure_method.sun_glass_create"
                        : "tobacconistmod.cure_method.sun_create");
            }
            return Component.translatable(isGlassSunCure(level, worldPosition)
                    ? "tobacconistmod.cure_method.sun_glass"
                    : "tobacconistmod.cure_method.sun_direct");
        }
        if (canAirDry(level, worldPosition) || fanAssist == CreateCompat.FanCuringAssist.AIR) {
            if (fanAssist == CreateCompat.FanCuringAssist.AIR) {
                return Component.translatable("tobacconistmod.cure_method.air_create");
            }
            return Component.translatable(!level.canSeeSky(worldPosition.above())
                    ? "tobacconistmod.cure_method.air_cover"
                    : "tobacconistmod.cure_method.air_open");
        }
        return Component.translatable(directRain
                ? "tobacconistmod.cure_method.paused_rain"
                : "tobacconistmod.cure_method.paused_unsuitable");
    }

    public boolean isDryingActive() {
        if (level == null || !hasLeaves() || isFinished()) {
            return false;
        }

        CreateCompat.FanCuringAssist fanAssist = getCreateFanAssist();
        boolean directRain = level.isRainingAt(worldPosition.above()) && level.canSeeSky(worldPosition.above());

        return isOverLitCampfire(level, worldPosition)
                || canFlueCure(level, worldPosition)
                || hasDirectSunlight(level, worldPosition)
                || canAirDry(level, worldPosition)
                || (!directRain && fanAssist != CreateCompat.FanCuringAssist.NONE);
    }

    public boolean isFinished() {
        if (storedLeaf.isEmpty()) {
            return false;
        }

        return LegacyItemTags.hasTag(storedLeaf)
                && LegacyItemTags.getTag(storedLeaf) != null
                && LegacyItemTags.getTag(storedLeaf).contains(TobaccoCuringHelper.TAG_CURE_TYPE);
    }

    public ItemStack removeAllLeaves() {
        if (storedLeaf.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack out = storedLeaf.copy();
        clearRack();
        syncRackState();
        syncToClient();
        return out;
    }

    public void dropContents(Level level, BlockPos pos) {
        if (!storedLeaf.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), storedLeaf);
            clearRack();
            syncRackState();
            syncToClient();
        }
    }

    private void clearRack() {
        storedLeaf = ItemStack.EMPTY;
        dryingProgress = 0;
        sunExposureTicks = 0;
        interruptionCount = 0;
        usedFireDrying = false;
        usedFlueDrying = false;
        lastTickHadValidDrying = false;
        directRainExposureTicks = 0;
        wetDamagePenalty = 0;
        airTicks = 0;
        sunTicks = 0;
        fireTicks = 0;
        flueTicks = 0;
        createFanAssistRefresh = 0;
        cachedCreateFanAssist = CreateCompat.FanCuringAssist.NONE;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TobaccoDryingRackBlockEntity rack) {
        if (level.isClientSide) {
            return;
        }

        boolean overCampfire = isOverLitCampfire(level, pos);
        boolean flueCure = canFlueCure(level, pos);
        boolean inSun = hasDirectSunlight(level, pos);
        boolean airDry = canAirDry(level, pos);

        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(TobaccoDryingRackBlock.OVER_CAMPFIRE)) {
            boolean currentValue = current.getValue(TobaccoDryingRackBlock.OVER_CAMPFIRE);
            if (currentValue != overCampfire) {
                level.setBlock(pos, current.setValue(TobaccoDryingRackBlock.OVER_CAMPFIRE, overCampfire), 3);
            }
        }

        // Synchronize visual load/cure state with the stored batch.
        rack.syncRackState();

        if (!rack.hasLeaves()) {
            rack.lastTickHadValidDrying = false;
            return;
        }

        boolean directRain = level.isRainingAt(pos.above()) && level.canSeeSky(pos.above());

        if (directRain) {
            rack.directRainExposureTicks++;

            if (rack.directRainExposureTicks % 200 == 0) {
                rack.wetDamagePenalty++;
            }

            if (rack.directRainExposureTicks >= 1200) {
                rack.ruinFromRain();
                return;
            }
        } else {
            rack.directRainExposureTicks = Math.max(0, rack.directRainExposureTicks - 5);
        }

        CreateCompat.FanCuringAssist fanAssist = directRain
                ? CreateCompat.FanCuringAssist.NONE
                : rack.getCreateFanAssist();

        boolean validDryingThisTick = false;
        int progressTicks = 0;

        // Cure precedence is Fire, Flue, Sun, then Air. Plain fan airflow can accelerate a valid Sun cure.
        if (overCampfire || fanAssist == CreateCompat.FanCuringAssist.FIRE) {
            validDryingThisTick = true;
            rack.usedFireDrying = true;
            progressTicks = fanAssist == CreateCompat.FanCuringAssist.FIRE
                    ? CREATE_FAN_HEATED_TICK_RATE
                    : 1;
            rack.fireTicks += progressTicks;
        } else if (flueCure || fanAssist == CreateCompat.FanCuringAssist.FLUE) {
            validDryingThisTick = true;
            rack.usedFlueDrying = true;
            progressTicks = fanAssist == CreateCompat.FanCuringAssist.FLUE
                    ? CREATE_FAN_HEATED_TICK_RATE
                    : 1;
            rack.flueTicks += progressTicks;
        } else if (inSun) {
            validDryingThisTick = true;
            progressTicks = fanAssist == CreateCompat.FanCuringAssist.AIR
                    ? CREATE_FAN_AIR_TICK_RATE
                    : 1;
            rack.sunTicks += progressTicks;
            rack.sunExposureTicks += progressTicks;
        } else if (airDry || fanAssist == CreateCompat.FanCuringAssist.AIR) {
            validDryingThisTick = true;
            progressTicks = fanAssist == CreateCompat.FanCuringAssist.AIR
                    ? CREATE_FAN_AIR_TICK_RATE
                    : 1;
            rack.airTicks += progressTicks;
        }

        if (rack.lastTickHadValidDrying && !validDryingThisTick) {
            rack.interruptionCount++;
            rack.syncToClient();
        }

        rack.lastTickHadValidDrying = validDryingThisTick;

        if (!validDryingThisTick) {
            return;
        }

        rack.dryingProgress += progressTicks;
        rack.syncRackState();

        int requiredSunTime = isGlassSunCure(level, pos) ? GLASS_SUN_DRY_TIME : SUN_DRY_TIME;

        if (rack.fireTicks >= FIRE_DRY_TIME
                || rack.flueTicks >= FLUE_DRY_TIME
                || rack.sunTicks >= requiredSunTime
                || rack.airTicks >= AIR_DRY_TIME) {
            rack.finishCuring();
            return;
        }

        if (level.getGameTime() % 20 == 0) {
            rack.syncToClient();
        } else {
            rack.setChanged();
        }
    }

    public void debugAddTime(int ticks) {
        if (ticks <= 0 || !hasLeaves() || isFinished()) {
            return;
        }

        if (level == null || level.isClientSide) {
            return;
        }

        boolean overCampfire = isOverLitCampfire(level, worldPosition);
        boolean flueCure = canFlueCure(level, worldPosition);
        boolean inSun = hasDirectSunlight(level, worldPosition);
        boolean airDry = canAirDry(level, worldPosition);
        boolean directRain = level.isRainingAt(worldPosition.above())
                && level.canSeeSky(worldPosition.above());
        CreateCompat.FanCuringAssist fanAssist = directRain
                ? CreateCompat.FanCuringAssist.NONE
                : getCreateFanAssist();

        if (overCampfire || fanAssist == CreateCompat.FanCuringAssist.FIRE) {
            usedFireDrying = true;
            fireTicks += ticks;
        } else if (flueCure || fanAssist == CreateCompat.FanCuringAssist.FLUE) {
            usedFlueDrying = true;
            flueTicks += ticks;
        } else if (inSun) {
            sunTicks += ticks;
            sunExposureTicks += ticks;
        } else if (airDry || fanAssist == CreateCompat.FanCuringAssist.AIR) {
            airTicks += ticks;
        }

        dryingProgress += ticks;
        syncRackState();

        int requiredSunTime = isGlassSunCure(level, worldPosition) ? GLASS_SUN_DRY_TIME : SUN_DRY_TIME;

        if (fireTicks >= FIRE_DRY_TIME
                || flueTicks >= FLUE_DRY_TIME
                || sunTicks >= requiredSunTime
                || airTicks >= AIR_DRY_TIME) {
            finishCuring();
            return;
        }

        syncToClient();
    }

    public void debugFinishNow() {
        if (!hasLeaves() || isFinished() || level == null || level.isClientSide) {
            return;
        }

        boolean directRain = level.isRainingAt(worldPosition.above())
                && level.canSeeSky(worldPosition.above());
        CreateCompat.FanCuringAssist fanAssist = directRain
                ? CreateCompat.FanCuringAssist.NONE
                : getCreateFanAssist();

        if (isOverLitCampfire(level, worldPosition) || fanAssist == CreateCompat.FanCuringAssist.FIRE) {
            usedFireDrying = true;
            fireTicks = FIRE_DRY_TIME;
        } else if (canFlueCure(level, worldPosition) || fanAssist == CreateCompat.FanCuringAssist.FLUE) {
            usedFlueDrying = true;
            flueTicks = FLUE_DRY_TIME;
        } else if (hasDirectSunlight(level, worldPosition)) {
            int requiredSunTime = isGlassSunCure(level, worldPosition) ? GLASS_SUN_DRY_TIME : SUN_DRY_TIME;
            sunTicks = requiredSunTime;
            sunExposureTicks = requiredSunTime;
        } else {
            airTicks = AIR_DRY_TIME;
        }

        finishCuring();
    }

    private void finishCuring() {
        if (storedLeaf.isEmpty()) {
            return;
        }

        ItemStack cured = TobaccoCuringHelper.getCuredLeafForRaw(storedLeaf);
        if (cured.isEmpty()) {
            return;
        }

        cured.setCount(storedLeaf.getCount());

        int dominant = Math.max(
                Math.max(fireTicks, sunTicks),
                Math.max(airTicks, flueTicks)
        );

        String cureType;
        if (dominant == fireTicks) {
            cureType = TobaccoCuringHelper.CURE_FIRE;
        } else if (dominant == flueTicks) {
            cureType = TobaccoCuringHelper.CURE_FLUE;
        } else if (dominant == sunTicks) {
            cureType = TobaccoCuringHelper.CURE_SUN;
        } else {
            cureType = TobaccoCuringHelper.CURE_AIR;
        }

        int total = fireTicks + sunTicks + airTicks + flueTicks;
        float ratio = total > 0 ? (float) dominant / total : 1f;

        int mixPenalty = 0;
        if (ratio < 0.9f && ratio >= 0.7f) {
            mixPenalty = 3;
        } else if (ratio < 0.7f && ratio >= 0.5f) {
            mixPenalty = 7;
        } else if (ratio < 0.5f) {
            mixPenalty = 12;
        }

        CompoundTag sourceTag = LegacyItemTags.getOrCreateTag(storedLeaf);
        int growth = sourceTag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)
                ? sourceTag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)
                : 50;

        int methodsUsed = 0;
        if (fireTicks > 0) methodsUsed++;
        if (flueTicks > 0) methodsUsed++;
        if (sunTicks > 0) methodsUsed++;
        if (airTicks > 0) methodsUsed++;

        boolean mixedMethods = methodsUsed > 1;
        boolean properEnvironment = interruptionCount == 0 && wetDamagePenalty == 0;
        int rng = level != null ? level.random.nextInt(11) : 0;

        int quality = TobaccoCuringHelper.buildFinalQuality(
                growth,
                cureType,
                interruptionCount,
                mixedMethods,
                properEnvironment,
                rng
        );

        quality -= Math.min(15, mixPenalty);
        quality -= Math.min(20, wetDamagePenalty);
        quality = TobaccoCuringHelper.clampQuality(Math.min(100, quality));

        TobaccoCuringHelper.applyCureData(cured, cureType, quality);
        // Cured stacks store final Quality rather than GrowthQuality.
        LegacyItemTags.getOrCreateTag(cured).remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);

        storedLeaf = cured;

        dryingProgress = 0;
        sunExposureTicks = 0;
        interruptionCount = 0;
        lastTickHadValidDrying = false;
        usedFireDrying = false;
        usedFlueDrying = false;
        directRainExposureTicks = 0;
        wetDamagePenalty = 0;
        airTicks = 0;
        sunTicks = 0;
        fireTicks = 0;
        flueTicks = 0;

        syncRackState();
        syncToClient();
    }

    private void syncRackState() {
        if (level == null) {
            return;
        }

        BlockState state = level.getBlockState(worldPosition);
        if (!state.is(ModBlocks.TOBACCO_DRYING_RACK.get())) {
            return;
        }

        BlockState updated = state;
        boolean changed = false;

        if (updated.hasProperty(TobaccoDryingRackBlock.HAS_LEAVES)) {
            boolean shouldHaveLeaves = hasLeaves();
            if (updated.getValue(TobaccoDryingRackBlock.HAS_LEAVES) != shouldHaveLeaves) {
                updated = updated.setValue(TobaccoDryingRackBlock.HAS_LEAVES, shouldHaveLeaves);
                changed = true;
            }
        }

        if (updated.hasProperty(TobaccoDryingRackBlock.LOAD_STAGE)) {
            int visualStage = getVisualLoadStage();
            if (updated.getValue(TobaccoDryingRackBlock.LOAD_STAGE) != visualStage) {
                updated = updated.setValue(TobaccoDryingRackBlock.LOAD_STAGE, visualStage);
                changed = true;
            }
        }

        if (updated.hasProperty(TobaccoDryingRackBlock.CURE_STAGE)) {
            int cureStage = getVisualCureStage();
            if (updated.getValue(TobaccoDryingRackBlock.CURE_STAGE) != cureStage) {
                updated = updated.setValue(TobaccoDryingRackBlock.CURE_STAGE, cureStage);
                changed = true;
            }
        }

        if (updated.hasProperty(TobaccoDryingRackBlock.VARIETY)) {
            int variety = hasLeaves()
                    ? com.diggydwarff.tobacconistmod.block.custom.HangingTobaccoBlock.getVarietyIndex(storedLeaf)
                    : 0;
            if (updated.getValue(TobaccoDryingRackBlock.VARIETY) != variety) {
                updated = updated.setValue(TobaccoDryingRackBlock.VARIETY, variety);
                changed = true;
            }
        }

        if (changed) {
            level.setBlock(worldPosition, updated, 3);
        }
    }

    public boolean isBatchLocked() {
        return !storedLeaf.isEmpty() && !isFinished() && getDryProgressPercent() >= 10;
    }

    private CreateCompat.FanCuringAssist getCreateFanAssist() {
        if (level == null) {
            return CreateCompat.FanCuringAssist.NONE;
        }

        if (createFanAssistRefresh-- <= 0) {
            cachedCreateFanAssist = CreateCompat.getFanCuringAssist(level, worldPosition);
            createFanAssistRefresh = CREATE_FAN_ASSIST_REFRESH_TICKS;
        }

        return cachedCreateFanAssist;
    }

    private static boolean isOverLitCampfire(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.getBlock() instanceof CampfireBlock
                && belowState.hasProperty(CampfireBlock.LIT)
                && belowState.getValue(CampfireBlock.LIT);
    }

    private static boolean canAirDry(Level level, BlockPos pos) {
        if (level.isRainingAt(pos.above())) {
            return false;
        }

        return level.getBrightness(LightLayer.SKY, pos.above()) >= 8;
    }

    private static boolean hasDirectSunlight(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();

        if (level.isRainingAt(abovePos) && level.canSeeSky(abovePos)) {
            return false;
        }

        if (!level.isDay()) {
            return false;
        }

        if (level.getBrightness(LightLayer.SKY, abovePos) < 14) {
            return false;
        }

        if (!isOpenAirSunStructure(level, pos)) {
            return false;
        }

        return level.canSeeSky(abovePos) || isGlassRoof(level.getBlockState(abovePos));
    }

    private static boolean canFlueCure(Level level, BlockPos pos) {
        if (level.isRainingAt(pos.above())) {
            return false;
        }

        if (isOverLitCampfire(level, pos)) {
            return false;
        }

        if (level.canSeeSky(pos.above())) {
            return false;
        }

        if (!hasClearAirAbove(level, pos)) {
            return false;
        }

        if (!hasRoofOverhead(level, pos)) {
            return false;
        }

        if (hasSmokeContaminationNearby(level, pos)) {
            return false;
        }

        return countNearbyFlueHeatSources(level, pos) >= 1;
    }

    private static boolean hasClearAirAbove(Level level, BlockPos pos) {
        for (int y = 1; y <= 2; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasRoofOverhead(Level level, BlockPos pos) {
        for (int y = 3; y <= 5; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState state = level.getBlockState(checkPos);

            if (state.isFaceSturdy(level, checkPos, Direction.DOWN)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSmokeContaminationNearby(Level level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos check = pos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(check);
                    Block block = state.getBlock();

                    if (block == Blocks.FIRE
                            || block == Blocks.SOUL_FIRE
                            || block == Blocks.CAMPFIRE
                            || block == Blocks.SOUL_CAMPFIRE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static int countNearbyFlueHeatSources(Level level, BlockPos pos) {
        int count = 0;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                int manhattan = Math.abs(dx) + Math.abs(dz);
                if (manhattan > 3) {
                    continue;
                }

                BlockPos sameLevel = pos.offset(dx, 0, dz);
                BlockPos belowLevel = pos.offset(dx, -1, dz);

                if (isFlueHeatSource(level, sameLevel) || isFlueHeatSource(level, belowLevel)) {
                    count++;
                }
            }
        }

        return count;
    }

    private static boolean isFlueHeatSource(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == ModBlocks.FLUE_FIREBOX.get()) {
            return state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);
        }

        return false;
    }

    private static boolean isGlassRoof(BlockState state) {
        return state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.WHITE_STAINED_GLASS)
                || state.is(Blocks.ORANGE_STAINED_GLASS)
                || state.is(Blocks.MAGENTA_STAINED_GLASS)
                || state.is(Blocks.LIGHT_BLUE_STAINED_GLASS)
                || state.is(Blocks.YELLOW_STAINED_GLASS)
                || state.is(Blocks.LIME_STAINED_GLASS)
                || state.is(Blocks.PINK_STAINED_GLASS)
                || state.is(Blocks.GRAY_STAINED_GLASS)
                || state.is(Blocks.LIGHT_GRAY_STAINED_GLASS)
                || state.is(Blocks.CYAN_STAINED_GLASS)
                || state.is(Blocks.PURPLE_STAINED_GLASS)
                || state.is(Blocks.BLUE_STAINED_GLASS)
                || state.is(Blocks.BROWN_STAINED_GLASS)
                || state.is(Blocks.GREEN_STAINED_GLASS)
                || state.is(Blocks.RED_STAINED_GLASS)
                || state.is(Blocks.BLACK_STAINED_GLASS)
                || state.is(Blocks.WHITE_STAINED_GLASS_PANE)
                || state.is(Blocks.ORANGE_STAINED_GLASS_PANE)
                || state.is(Blocks.MAGENTA_STAINED_GLASS_PANE)
                || state.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
                || state.is(Blocks.YELLOW_STAINED_GLASS_PANE)
                || state.is(Blocks.LIME_STAINED_GLASS_PANE)
                || state.is(Blocks.PINK_STAINED_GLASS_PANE)
                || state.is(Blocks.GRAY_STAINED_GLASS_PANE)
                || state.is(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
                || state.is(Blocks.CYAN_STAINED_GLASS_PANE)
                || state.is(Blocks.PURPLE_STAINED_GLASS_PANE)
                || state.is(Blocks.BLUE_STAINED_GLASS_PANE)
                || state.is(Blocks.BROWN_STAINED_GLASS_PANE)
                || state.is(Blocks.GREEN_STAINED_GLASS_PANE)
                || state.is(Blocks.RED_STAINED_GLASS_PANE)
                || state.is(Blocks.BLACK_STAINED_GLASS_PANE);
    }

    private static boolean isOpenAirSunStructure(Level level, BlockPos pos) {
        int openSides = 0;

        if (isOpenSide(level, pos.north())) openSides++;
        if (isOpenSide(level, pos.south())) openSides++;
        if (isOpenSide(level, pos.east())) openSides++;
        if (isOpenSide(level, pos.west())) openSides++;

        return openSides >= 2;
    }

    private static boolean isOpenSide(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir();
    }

    private static boolean isGlassSunCure(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return !level.canSeeSky(abovePos) && isGlassRoof(level.getBlockState(abovePos)) && isOpenAirSunStructure(level, pos);
    }

    private boolean isCurrentlyGlassSunCuring() {
        return level != null && hasLeaves() && isGlassSunCure(level, worldPosition);
    }

    private boolean isValidLeaf(ItemStack stack) {
        return stack.is(ModItems.WILD_TOBACCO_LEAF.get())
                || stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get())
                || stack.is(ModItems.BURLEY_TOBACCO_LEAF.get())
                || stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get())
                || stack.is(ModItems.DOKHA_TOBACCO_LEAF.get())
                || stack.is(ModItems.SHADE_TOBACCO_LEAF.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (!storedLeaf.isEmpty()) {
            tag.put("StoredLeaf", storedLeaf.save(registries, new CompoundTag()));
        }

        tag.putInt("DryingProgress", dryingProgress);
        tag.putInt("SunExposureTicks", sunExposureTicks);
        tag.putInt("InterruptionCount", interruptionCount);
        tag.putBoolean("LastTickHadValidDrying", lastTickHadValidDrying);
        tag.putBoolean("UsedFireDrying", usedFireDrying);
        tag.putBoolean("UsedFlueDrying", usedFlueDrying);
        tag.putInt("DirectRainExposureTicks", directRainExposureTicks);
        tag.putInt("WetDamagePenalty", wetDamagePenalty);
        tag.putInt("AirTicks", airTicks);
        tag.putInt("SunTicks", sunTicks);
        tag.putInt("FireTicks", fireTicks);
        tag.putInt("FlueTicks", flueTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("StoredLeaf")) {
            storedLeaf = ItemStack.parseOptional(registries, tag.getCompound("StoredLeaf"));
        } else {
            storedLeaf = ItemStack.EMPTY;
        }

        dryingProgress = tag.getInt("DryingProgress");
        sunExposureTicks = tag.getInt("SunExposureTicks");
        interruptionCount = tag.getInt("InterruptionCount");
        lastTickHadValidDrying = tag.getBoolean("LastTickHadValidDrying");
        usedFireDrying = tag.getBoolean("UsedFireDrying");
        usedFlueDrying = tag.getBoolean("UsedFlueDrying");
        directRainExposureTicks = tag.getInt("DirectRainExposureTicks");
        wetDamagePenalty = tag.getInt("WetDamagePenalty");
        airTicks = tag.getInt("AirTicks");
        sunTicks = tag.getInt("SunTicks");
        fireTicks = tag.getInt("FireTicks");
        flueTicks = tag.getInt("FlueTicks");
    }

    public List<Component> getFullDebugLines() {
        Component itemName = storedLeaf.isEmpty()
                ? Component.translatable("tobacconistmod.ui.empty")
                : Component.translatable("tobacconistmod.ui.item_count", storedLeaf.getHoverName(), storedLeaf.getCount());

        int light = level != null ? level.getBrightness(LightLayer.SKY, worldPosition.above()) : 0;
        boolean raining = level != null && level.isRainingAt(worldPosition.above());
        boolean glassSun = level != null && isGlassSunCure(level, worldPosition);
        boolean openAirSun = level != null && isOpenAirSunStructure(level, worldPosition);

        return List.of(
                Component.translatable("tobacconistmod.debug.drying_rack_title").withStyle(ChatFormatting.GOLD),
                Component.translatable("tobacconistmod.debug.stored", itemName),
                Component.translatable("tobacconistmod.debug.method", getCurrentCureMethodComponent()),
                Component.translatable("tobacconistmod.debug.progress", getDryProgressPercent()),
                Component.translatable("tobacconistmod.debug.air_ticks", airTicks),
                Component.translatable("tobacconistmod.debug.sun_ticks", sunTicks),
                Component.translatable("tobacconistmod.debug.fire_ticks", fireTicks),
                Component.translatable("tobacconistmod.debug.flue_ticks", flueTicks),
                Component.translatable("tobacconistmod.debug.direct_rain_exposure", directRainExposureTicks),
                Component.translatable("tobacconistmod.debug.wet_damage_penalty", wetDamagePenalty),
                Component.translatable("tobacconistmod.debug.interruptions", interruptionCount),
                Component.translatable("tobacconistmod.debug.sun_exposure", sunExposureTicks),
                Component.translatable("tobacconistmod.debug.light", light),
                Component.translatable("tobacconistmod.debug.raining", TobaccoText.yesNo(raining)),
                Component.translatable("tobacconistmod.debug.glass_sun_cure", TobaccoText.yesNo(glassSun)),
                Component.translatable("tobacconistmod.debug.open_air_sun_structure", TobaccoText.yesNo(openAirSun)),
                Component.translatable("tobacconistmod.debug.finished", TobaccoText.yesNo(isFinished())),
                Component.translatable("tobacconistmod.debug.batch_locked", TobaccoText.yesNo(isBatchLocked()))
        );
    }


    private void ruinFromRain() {
        if (storedLeaf.isEmpty()) {
            return;
        }

        ItemStack ruined = new ItemStack(ModItems.SPOILED_TOBACCO.get(), storedLeaf.getCount());

        if (LegacyItemTags.hasTag(storedLeaf)) {
            LegacyItemTags.setTag(ruined, LegacyItemTags.getTag(storedLeaf).copy());
        }

        CompoundTag tag = LegacyItemTags.getOrCreateTag(ruined);
        tag.putBoolean("Ruined", true);

        int quality = TobaccoCuringHelper.getQuality(storedLeaf);
        int ruinedQuality = Math.max(0, quality - 20);

        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, ruinedQuality);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(ruinedQuality));

        storedLeaf = ruined;

        dryingProgress = 0;
        sunExposureTicks = 0;
        interruptionCount = 0;
        lastTickHadValidDrying = false;
        usedFireDrying = false;
        usedFlueDrying = false;
        airTicks = 0;
        sunTicks = 0;
        fireTicks = 0;
        flueTicks = 0;
        directRainExposureTicks = 0;
        wetDamagePenalty = 0;

        syncRackState();
        syncToClient();
    }
}