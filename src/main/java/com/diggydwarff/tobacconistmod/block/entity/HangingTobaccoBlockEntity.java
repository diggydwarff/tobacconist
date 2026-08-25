package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.HangingTobaccoBlock;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * A traditional 16-leaf tobacco bunch hung directly from the underside of a roof/beam.
 *
 * <p>The upper block owns this block entity; the lower half is visual/physical only. Curing
 * intentionally mirrors the Drying Rack's timers, quality calculation, priority order and
 * Create-fan assistance, but the environment checks are adapted to a hanging bundle: sun comes
 * through side skylight/pergola gaps because direct sky above the attachment point is impossible.</p>
 */
public class HangingTobaccoBlockEntity extends BlockEntity {

    public static final int LEAF_COUNT = 16;

    private static final int CREATE_FAN_AIR_TICK_RATE = 4;
    private static final int CREATE_FAN_HEATED_TICK_RATE = 6;
    private static final int CREATE_FAN_ASSIST_REFRESH_TICKS = 10;

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
    // Persist the botanical variety independently of cure state so a bunch that cures in place
    // always switches to its own finished variety model/texture.
    private int tobaccoVariety = 0;

    private int createFanAssistRefresh = 0;
    private CreateCompat.FanCuringAssist cachedCreateFanAssist = CreateCompat.FanCuringAssist.NONE;

    public HangingTobaccoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HANGING_TOBACCO.get(), pos, state);
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

    public void setLeaves(ItemStack source) {
        if (source.isEmpty() || (!TobaccoCuringHelper.isRawTobaccoLeaf(source)
                && !TobaccoCuringHelper.isDryTobaccoLeaf(source))) {
            return;
        }

        storedLeaf = source.copyWithCount(LEAF_COUNT);
        tobaccoVariety = HangingTobaccoBlock.getVarietyIndex(source);
        resetProgress();
        syncState();
        syncToClient();
    }

    public void discardContents() {
        storedLeaf = ItemStack.EMPTY;
        tobaccoVariety = 0;
        resetProgress();
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        if (storedLeaf.isEmpty()) {
            return;
        }

        Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, storedLeaf.copy());
        storedLeaf = ItemStack.EMPTY;
        tobaccoVariety = 0;
        resetProgress();
        setChanged();
    }

    public boolean isFinished() {
        if (!hasLeaves()) return false;
        if (TobaccoCuringHelper.isDryTobaccoLeaf(storedLeaf)) return true;
        if (!LegacyItemTags.hasTag(storedLeaf)) return false;
        CompoundTag tag = LegacyItemTags.getTag(storedLeaf);
        return tag != null && tag.contains(TobaccoCuringHelper.TAG_CURE_TYPE);
    }

    public int getDryProgressPercent() {
        if (!hasLeaves()) return 0;
        if (isFinished()) return 100;

        int tracked;
        int needed = getRequiredDryingTime();
        if (usedFireDrying) {
            tracked = fireTicks;
        } else if (usedFlueDrying) {
            tracked = flueTicks;
        } else if (sunTicks > 0) {
            tracked = sunTicks;
        } else {
            tracked = airTicks;
        }
        return needed <= 0 ? 0 : Math.min(100, (tracked * 100) / needed);
    }

    public int getVisualCureStage() {
        if (!hasLeaves()) return 0;
        if (isFinished()) return 5;

        int pct = getDryProgressPercent();
        if (pct >= 80) return 4;
        if (pct >= 60) return 3;
        if (pct >= 40) return 2;
        if (pct >= 20) return 1;
        return 0;
    }

    public int getEstimatedTicksRemaining() {
        if (!hasLeaves() || isFinished()) return 0;
        int required = getRequiredDryingTime();
        int pct = getDryProgressPercent();
        int remainingCureTicks = Math.max(0,
                (int) Math.ceil(required * ((100.0D - pct) / 100.0D)));
        return Math.max(0, (int) Math.ceil((double) remainingCureTicks / getCurrentDryingTickRate()));
    }

    private int getRequiredDryingTime() {
        if (usedFireDrying) {
            return TobaccoDryingRackBlockEntity.FIRE_DRY_TIME;
        }
        if (usedFlueDrying) {
            return TobaccoDryingRackBlockEntity.FLUE_DRY_TIME;
        }
        if (sunTicks > 0) {
            return TobaccoDryingRackBlockEntity.SUN_DRY_TIME;
        }
        return TobaccoDryingRackBlockEntity.AIR_DRY_TIME;
    }

    private boolean isFirePriority(CreateCompat.FanCuringAssist fan) {
        return level != null && (isOverLitCampfire(level, worldPosition) || fan == CreateCompat.FanCuringAssist.FIRE || usedFireDrying);
    }

    private boolean isFluePriority(CreateCompat.FanCuringAssist fan) {
        return level != null && !isFirePriority(fan) && (canFlueCure(level, worldPosition) || fan == CreateCompat.FanCuringAssist.FLUE || usedFlueDrying);
    }

    private int getCurrentDryingTickRate() {
        if (level == null || isSideRainExposed(level, worldPosition)) return 1;

        CreateCompat.FanCuringAssist fan = getCreateFanAssist();
        if (fan == CreateCompat.FanCuringAssist.FIRE || fan == CreateCompat.FanCuringAssist.FLUE) {
            return CREATE_FAN_HEATED_TICK_RATE;
        }
        if (fan == CreateCompat.FanCuringAssist.AIR) {
            return CREATE_FAN_AIR_TICK_RATE;
        }
        return 1;
    }

    public boolean isDryingActive() {
        if (level == null || !hasLeaves() || isFinished()) return false;
        if (isSideRainExposed(level, worldPosition)) return false;

        CreateCompat.FanCuringAssist fan = getCreateFanAssist();
        return isOverLitCampfire(level, worldPosition)
                || canFlueCure(level, worldPosition)
                || hasPergolaSunlight(level, worldPosition)
                || canAirDry(level, worldPosition)
                || fan != CreateCompat.FanCuringAssist.NONE;
    }

    public String getCurrentCureMethod() {
        if (level == null || !hasLeaves()) return "Empty";
        if (isFinished()) {
            return TobaccoCuringHelper.getCureDisplayName(TobaccoCuringHelper.getCureType(storedLeaf));
        }

        boolean rain = isSideRainExposed(level, worldPosition);
        CreateCompat.FanCuringAssist fan = rain ? CreateCompat.FanCuringAssist.NONE : getCreateFanAssist();

        if (isFirePriority(fan)) {
            return fan == CreateCompat.FanCuringAssist.FIRE
                    ? "Fire-curing (Create smoke airflow)"
                    : "Fire-curing (campfire smoke)";
        }
        if (isFluePriority(fan)) {
            return fan == CreateCompat.FanCuringAssist.FLUE
                    ? "Flue-curing (Create heated airflow)"
                    : "Flue-curing (indirect barn heat)";
        }
        if (hasPergolaSunlight(level, worldPosition)) {
            return fan == CreateCompat.FanCuringAssist.AIR
                    ? "Sun-curing (pergola light, fan-assisted)"
                    : "Sun-curing (pergola/side light)";
        }
        if (canAirDry(level, worldPosition) || fan == CreateCompat.FanCuringAssist.AIR) {
            return fan == CreateCompat.FanCuringAssist.AIR
                    ? "Air-curing (fan-assisted)"
                    : "Air-curing (covered)";
        }
        if (rain) return "Paused (rain through side opening)";
        return "Paused (unsuitable conditions)";
    }

    public String getStatusText() {
        if (!hasLeaves()) return "Empty";
        if (isFinished()) {
            return "Finished - " + TobaccoCuringHelper.getCureDisplayName(TobaccoCuringHelper.getCureType(storedLeaf));
        }
        return getCurrentCureMethod() + " - " + getDryProgressPercent() + "%";
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HangingTobaccoBlockEntity bundle) {
        if (level.isClientSide || !bundle.hasLeaves()) return;

        bundle.syncState();
        if (bundle.isFinished()) return;

        boolean rain = isSideRainExposed(level, pos);
        if (rain) {
            bundle.directRainExposureTicks++;
            if (bundle.directRainExposureTicks % 200 == 0) {
                bundle.wetDamagePenalty++;
            }
            if (bundle.directRainExposureTicks >= 1200) {
                bundle.ruinFromRain();
                return;
            }
        } else {
            bundle.directRainExposureTicks = Math.max(0, bundle.directRainExposureTicks - 5);
        }

        CreateCompat.FanCuringAssist fan = rain
                ? CreateCompat.FanCuringAssist.NONE
                : bundle.getCreateFanAssist();
        boolean fire = isOverLitCampfire(level, pos);
        boolean flue = canFlueCure(level, pos);
        boolean sun = hasPergolaSunlight(level, pos);
        boolean air = canAirDry(level, pos);

        boolean valid = false;
        int progressTicks = 0;

        if (fire || fan == CreateCompat.FanCuringAssist.FIRE) {
            valid = true;
            bundle.usedFireDrying = true;
            progressTicks = fan == CreateCompat.FanCuringAssist.FIRE ? CREATE_FAN_HEATED_TICK_RATE : 1;
            bundle.fireTicks += progressTicks;
        } else if (flue || fan == CreateCompat.FanCuringAssist.FLUE) {
            valid = true;
            bundle.usedFlueDrying = true;
            progressTicks = fan == CreateCompat.FanCuringAssist.FLUE ? CREATE_FAN_HEATED_TICK_RATE : 1;
            bundle.flueTicks += progressTicks;
        } else if (sun) {
            valid = true;
            progressTicks = fan == CreateCompat.FanCuringAssist.AIR ? CREATE_FAN_AIR_TICK_RATE : 1;
            bundle.sunTicks += progressTicks;
            bundle.sunExposureTicks += progressTicks;
        } else if (air || fan == CreateCompat.FanCuringAssist.AIR) {
            valid = true;
            progressTicks = fan == CreateCompat.FanCuringAssist.AIR ? CREATE_FAN_AIR_TICK_RATE : 1;
            bundle.airTicks += progressTicks;
        }

        if (bundle.lastTickHadValidDrying && !valid) {
            bundle.interruptionCount++;
            bundle.syncToClient();
        }
        bundle.lastTickHadValidDrying = valid;

        if (!valid) return;

        bundle.dryingProgress += progressTicks;
        bundle.syncState();

        if (bundle.fireTicks >= TobaccoDryingRackBlockEntity.FIRE_DRY_TIME
                || bundle.flueTicks >= TobaccoDryingRackBlockEntity.FLUE_DRY_TIME
                || bundle.sunTicks >= TobaccoDryingRackBlockEntity.SUN_DRY_TIME
                || bundle.airTicks >= TobaccoDryingRackBlockEntity.AIR_DRY_TIME) {
            bundle.finishCuring();
            return;
        }

        if (level.getGameTime() % 20 == 0) {
            bundle.syncToClient();
        } else {
            bundle.setChanged();
        }
    }

    public void debugAddTime(int ticks) {
        if (ticks <= 0 || !hasLeaves() || isFinished() || level == null || level.isClientSide) return;

        CreateCompat.FanCuringAssist fan = isSideRainExposed(level, worldPosition)
                ? CreateCompat.FanCuringAssist.NONE
                : getCreateFanAssist();

        if (isOverLitCampfire(level, worldPosition) || fan == CreateCompat.FanCuringAssist.FIRE) {
            usedFireDrying = true;
            fireTicks += ticks;
        } else if (canFlueCure(level, worldPosition) || fan == CreateCompat.FanCuringAssist.FLUE) {
            usedFlueDrying = true;
            flueTicks += ticks;
        } else if (hasPergolaSunlight(level, worldPosition)) {
            sunTicks += ticks;
            sunExposureTicks += ticks;
        } else {
            airTicks += ticks;
        }

        dryingProgress += ticks;
        syncState();

        if (fireTicks >= TobaccoDryingRackBlockEntity.FIRE_DRY_TIME
                || flueTicks >= TobaccoDryingRackBlockEntity.FLUE_DRY_TIME
                || sunTicks >= TobaccoDryingRackBlockEntity.SUN_DRY_TIME
                || airTicks >= TobaccoDryingRackBlockEntity.AIR_DRY_TIME) {
            finishCuring();
        } else {
            syncToClient();
        }
    }

    public void debugFinishNow() {
        if (!hasLeaves() || isFinished() || level == null || level.isClientSide) return;

        CreateCompat.FanCuringAssist fan = isSideRainExposed(level, worldPosition)
                ? CreateCompat.FanCuringAssist.NONE
                : getCreateFanAssist();

        if (isOverLitCampfire(level, worldPosition) || fan == CreateCompat.FanCuringAssist.FIRE) {
            usedFireDrying = true;
            fireTicks = TobaccoDryingRackBlockEntity.FIRE_DRY_TIME;
        } else if (canFlueCure(level, worldPosition) || fan == CreateCompat.FanCuringAssist.FLUE) {
            usedFlueDrying = true;
            flueTicks = TobaccoDryingRackBlockEntity.FLUE_DRY_TIME;
        } else if (hasPergolaSunlight(level, worldPosition)) {
            sunTicks = TobaccoDryingRackBlockEntity.SUN_DRY_TIME;
            sunExposureTicks = TobaccoDryingRackBlockEntity.SUN_DRY_TIME;
        } else {
            airTicks = TobaccoDryingRackBlockEntity.AIR_DRY_TIME;
        }

        finishCuring();
    }

    private void finishCuring() {
        if (storedLeaf.isEmpty()) return;

        ItemStack cured = TobaccoCuringHelper.getCuredLeafForRaw(storedLeaf);
        if (cured.isEmpty()) return;
        cured.setCount(LEAF_COUNT);

        String cureType;
        int tracked;
        if (usedFireDrying) {
            cureType = TobaccoCuringHelper.CURE_FIRE;
            tracked = fireTicks;
        } else if (usedFlueDrying) {
            cureType = TobaccoCuringHelper.CURE_FLUE;
            tracked = flueTicks;
        } else if (sunTicks > 0) {
            cureType = TobaccoCuringHelper.CURE_SUN;
            tracked = sunTicks;
        } else {
            cureType = TobaccoCuringHelper.CURE_AIR;
            tracked = airTicks;
        }

        int total = fireTicks + sunTicks + airTicks + flueTicks;
        float ratio = total > 0 ? (float) tracked / total : 1.0F;
        int mixPenalty = ratio >= 0.9F ? 0 : ratio >= 0.7F ? 3 : ratio >= 0.5F ? 7 : 12;

        CompoundTag sourceTag = LegacyItemTags.getOrCreateTag(storedLeaf);
        int growth = sourceTag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)
                ? sourceTag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)
                : 50;

        int methodsUsed = 0;
        if (fireTicks > 0) methodsUsed++;
        if (flueTicks > 0) methodsUsed++;
        if (sunTicks > 0) methodsUsed++;
        if (airTicks > 0) methodsUsed++;

        int quality = TobaccoCuringHelper.buildFinalQuality(
                growth,
                cureType,
                interruptionCount,
                methodsUsed > 1,
                interruptionCount == 0 && wetDamagePenalty == 0,
                level != null ? level.random.nextInt(11) : 0
        );
        quality -= Math.min(15, mixPenalty);
        quality -= Math.min(20, wetDamagePenalty);
        quality = TobaccoCuringHelper.clampQuality(Math.min(100, quality));

        TobaccoCuringHelper.applyCureData(cured, cureType, quality);
        LegacyItemTags.getOrCreateTag(cured).remove(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
        storedLeaf = cured;
        resetProgress();
        syncState();
        syncToClient();
    }

    private void ruinFromRain() {
        if (storedLeaf.isEmpty()) return;

        ItemStack ruined = new ItemStack(ModItems.SPOILED_TOBACCO.get(), LEAF_COUNT);
        if (LegacyItemTags.hasTag(storedLeaf)) {
            LegacyItemTags.setTag(ruined, LegacyItemTags.getTag(storedLeaf).copy());
        }

        CompoundTag tag = LegacyItemTags.getOrCreateTag(ruined);
        tag.putBoolean("Ruined", true);
        int ruinedQuality = Math.max(0, TobaccoCuringHelper.getQuality(storedLeaf) - 20);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, ruinedQuality);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(ruinedQuality));

        storedLeaf = ruined;
        resetProgress();
        syncState();
        syncToClient();
    }

    private void resetProgress() {
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
        createFanAssistRefresh = 0;
        cachedCreateFanAssist = CreateCompat.FanCuringAssist.NONE;
    }

    private void syncState() {
        if (level == null) return;

        BlockState upper = level.getBlockState(worldPosition);
        if (!upper.is(ModBlocks.HANGING_TOBACCO_LEAVES.get())
                || upper.getValue(HangingTobaccoBlock.HALF) != DoubleBlockHalf.UPPER) {
            return;
        }

        int cureStage = getVisualCureStage();
        int variety = tobaccoVariety;
        if (upper.getValue(HangingTobaccoBlock.CURE_STAGE) != cureStage
                || upper.getValue(HangingTobaccoBlock.VARIETY) != variety) {
            level.setBlock(worldPosition, upper
                    .setValue(HangingTobaccoBlock.CURE_STAGE, cureStage)
                    .setValue(HangingTobaccoBlock.VARIETY, variety), 3);
        }

        BlockPos lowerPos = worldPosition.below();
        BlockState lower = level.getBlockState(lowerPos);
        if (lower.is(ModBlocks.HANGING_TOBACCO_LEAVES.get())
                && lower.getValue(HangingTobaccoBlock.HALF) == DoubleBlockHalf.LOWER
                && (lower.getValue(HangingTobaccoBlock.CURE_STAGE) != cureStage
                || lower.getValue(HangingTobaccoBlock.VARIETY) != variety)) {
            level.setBlock(lowerPos, lower
                    .setValue(HangingTobaccoBlock.CURE_STAGE, cureStage)
                    .setValue(HangingTobaccoBlock.VARIETY, variety), 3);
        }
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private CreateCompat.FanCuringAssist getCreateFanAssist() {
        if (level == null) return CreateCompat.FanCuringAssist.NONE;

        if (createFanAssistRefresh-- <= 0) {
            // Probe from the lower half: Create's resolver checks this position and the block
            // above it, covering both physical halves of the two-block hanging bundle.
            cachedCreateFanAssist = CreateCompat.getFanCuringAssist(level, worldPosition.below());
            createFanAssistRefresh = CREATE_FAN_ASSIST_REFRESH_TICKS;
        }
        return cachedCreateFanAssist;
    }

    /** Directly under the lower half of the two-block bunch. */
    private static boolean isOverLitCampfire(Level level, BlockPos upperPos) {
        BlockPos belowBundle = upperPos.below(2);
        BlockState state = level.getBlockState(belowBundle);
        return state.getBlock() instanceof CampfireBlock
                && state.hasProperty(CampfireBlock.LIT)
                && state.getValue(CampfireBlock.LIT);
    }

    /**
     * Side-sky sun check for pergolas/slatted curing structures.
     *
     * <p>The support block above a hanging bunch necessarily blocks vertical sky. Instead, one
     * horizontal neighboring column must be genuine open air with daytime sky light. This makes
     * long alternating beam/gap/beam/gap rows work naturally, while a fully solid roof remains
     * Air Curing.</p>
     */
    private static boolean hasPergolaSunlight(Level level, BlockPos upperPos) {
        if (!level.isDay() || level.isRaining()) return false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sideUpper = upperPos.relative(direction);
            BlockPos sideLower = upperPos.below().relative(direction);
            if (isOpenSkyChannel(level, sideUpper) || isOpenSkyChannel(level, sideLower)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOpenSkyChannel(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.canSeeSky(pos)
                && level.getBrightness(LightLayer.SKY, pos) >= 14;
    }

    private static boolean isSideRainExposed(Level level, BlockPos upperPos) {
        if (!level.isRaining()) return false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sideUpper = upperPos.relative(direction);
            BlockPos sideLower = upperPos.below().relative(direction);
            if ((level.getBlockState(sideUpper).isAir() && level.canSeeSky(sideUpper))
                    || (level.getBlockState(sideLower).isAir() && level.canSeeSky(sideLower))) {
                return true;
            }
        }
        return false;
    }

    /** Hanging tobacco under a solid roof naturally falls back to Air Curing. */
    private static boolean canAirDry(Level level, BlockPos upperPos) {
        return !isSideRainExposed(level, upperPos);
    }

    private static boolean canFlueCure(Level level, BlockPos upperPos) {
        BlockPos center = upperPos.below();
        if (isSideRainExposed(level, upperPos)) return false;
        if (isOverLitCampfire(level, upperPos)) return false;
        if (hasSmokeContaminationNearby(level, center)) return false;
        return countNearbyFlueHeatSources(level, center) >= 1;
    }

    private static boolean hasSmokeContaminationNearby(Level level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockState state = level.getBlockState(pos.offset(dx, dy, dz));
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
                if (dx == 0 && dz == 0) continue;
                if (Math.abs(dx) + Math.abs(dz) > 3) continue;

                BlockPos same = pos.offset(dx, 0, dz);
                BlockPos below = pos.offset(dx, -1, dz);
                if (isFlueHeatSource(level, same) || isFlueHeatSource(level, below)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isFlueHeatSource(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.FLUE_FIREBOX.get())
                && state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);
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
        tag.putInt("TobaccoVariety", tobaccoVariety);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedLeaf = tag.contains("StoredLeaf")
                ? ItemStack.parseOptional(registries, tag.getCompound("StoredLeaf"))
                : ItemStack.EMPTY;
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
        tobaccoVariety = tag.contains("TobaccoVariety")
                ? tag.getInt("TobaccoVariety")
                : HangingTobaccoBlock.getVarietyIndex(storedLeaf);
    }
}
