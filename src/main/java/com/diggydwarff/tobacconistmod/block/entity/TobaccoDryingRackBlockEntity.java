package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TobaccoDryingRackBlockEntity extends BlockEntity {

    public static final int MAX_LEAVES = 16;
    public static final int AIR_DRY_TIME = 72000;   // ~3 Minecraft days
    public static final int SUN_DRY_TIME = 48000;   // ~2 Minecraft days
    public static final int FIRE_DRY_TIME = 24000;  // ~1 Minecraft day
    public static final int FLUE_DRY_TIME = 36000;  // ~1.5 Minecraft days

    private ItemStack storedLeaf = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private int sunExposureTicks = 0;
    private int interruptionCount = 0;
    private boolean lastTickHadValidDrying = false;
    private boolean usedFireDrying = false;
    private boolean usedFlueDrying = false;

    private int airTicks = 0;
    private int sunTicks = 0;
    private int fireTicks = 0;
    private int flueTicks = 0;

    public TobaccoDryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOBACCO_DRYING_RACK.get(), pos, state);
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

        if (!ItemStack.isSameItemSameTags(storedLeaf, stack)) {
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
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }
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
            needed = SUN_DRY_TIME;
        } else {
            needed = AIR_DRY_TIME;
        }

        if (needed <= 0) {
            return 0;
        }

        return Math.min(100, (dominantTicks * 100) / needed);
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
            return SUN_DRY_TIME;
        }
        return AIR_DRY_TIME;
    }

    public String getRackStatusText() {
        if (!hasLeaves()) {
            return "Empty";
        }

        if (isFinished()) {
            String cureType = TobaccoCuringHelper.getCureType(storedLeaf);
            return "Finished - " + TobaccoCuringHelper.getCureDisplayName(cureType);
        }

        return getCurrentCureMethod() + " - " + getDryProgressPercent() + "%";
    }

    public boolean isDryingActive() {
        if (level == null || !hasLeaves() || isFinished()) {
            return false;
        }

        return isOverLitCampfire(level, worldPosition)
                || canFlueCure(level, worldPosition)
                || canAirDry(level, worldPosition);
    }

    public String getCurrentCureMethod() {
        if (level == null || !hasLeaves()) {
            return "Empty";
        }

        if (isFinished()) {
            return TobaccoCuringHelper.getCureDisplayName(TobaccoCuringHelper.getCureType(storedLeaf));
        }

        if (isOverLitCampfire(level, worldPosition)) {
            return "Fire-curing (campfire heat)";
        }

        if (canFlueCure(level, worldPosition)) {
            return "Flue-curing (indirect barn heat)";
        }

        if (hasDirectSunlight(level, worldPosition)) {
            return "Sun-curing (direct sunlight)";
        }

        if (canAirDry(level, worldPosition)) {
            if (!level.canSeeSky(worldPosition.above())) {
                return "Air-curing (under cover)";
            }
            return "Air-curing (open air)";
        }

        if (level.isRainingAt(worldPosition.above())) {
            return "Paused (rain exposure)";
        }

        return "Paused (unsuitable conditions)";
    }

    public boolean isFinished() {
        if (storedLeaf.isEmpty()) {
            return false;
        }

        return storedLeaf.hasTag()
                && storedLeaf.getTag() != null
                && storedLeaf.getTag().contains(TobaccoCuringHelper.TAG_CURE_TYPE);
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
        airTicks = 0;
        sunTicks = 0;
        fireTicks = 0;
        flueTicks = 0;
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

        if (!rack.hasLeaves()) {
            rack.lastTickHadValidDrying = false;
            return;
        }

        boolean validDryingThisTick = false;
        boolean shouldCountInterruption = false;

        if (overCampfire) {
            validDryingThisTick = true;
            rack.usedFireDrying = true;
            rack.fireTicks++;
        } else if (flueCure) {
            validDryingThisTick = true;
            rack.usedFlueDrying = true;
            rack.flueTicks++;
        } else if (inSun) {
            validDryingThisTick = true;
            rack.sunTicks++;
            rack.sunExposureTicks++;
        } else if (airDry) {
            validDryingThisTick = true;
            rack.airTicks++;

            // Only penalize if daytime sun curing is blocked, not night
            if (isDaytimeSunBlocked(level, pos)) {
                shouldCountInterruption = true;
            }
        } else {
            // Only count interruption when conditions are actually bad, not just night pause
            if (level.isRainingAt(pos.above())) {
                shouldCountInterruption = true;
            }
        }

        if (shouldCountInterruption && rack.lastTickHadValidDrying) {
            rack.interruptionCount++;
            rack.syncToClient();
        }

        rack.lastTickHadValidDrying = validDryingThisTick;

        if (!validDryingThisTick) {
            return;
        }

        rack.dryingProgress++;

        if (rack.fireTicks >= FIRE_DRY_TIME
                || rack.flueTicks >= FLUE_DRY_TIME
                || rack.sunTicks >= SUN_DRY_TIME
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

        int quality = TobaccoCuringHelper.buildFinalQuality(storedLeaf, cureType, interruptionCount);
        quality -= mixPenalty;

        TobaccoCuringHelper.applyCureData(cured, cureType, quality);

        storedLeaf = cured;

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

        syncRackState();
        syncToClient();
    }

    private static boolean isStrongSunHours(Level level) {
        long time = level.getDayTime() % 24000;
        return time >= 2000 && time <= 10000;
    }

    private void syncRackState() {
        if (level == null) {
            return;
        }

        BlockState state = level.getBlockState(worldPosition);
        if (!state.is(ModBlocks.TOBACCO_DRYING_RACK.get())) {
            return;
        }

        if (state.hasProperty(TobaccoDryingRackBlock.HAS_LEAVES)) {
            boolean current = state.getValue(TobaccoDryingRackBlock.HAS_LEAVES);
            boolean shouldBe = hasLeaves();
            if (current != shouldBe) {
                level.setBlock(worldPosition, state.setValue(TobaccoDryingRackBlock.HAS_LEAVES, shouldBe), 3);
            }
        }
    }

    public boolean isBatchLocked() {
        return !storedLeaf.isEmpty() && !isFinished() && getDryProgressPercent() >= 10;
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
        if (level.isRainingAt(pos.above())) {
            return false;
        }

        if (!level.isDay()) {
            return false;
        }

        if (!level.canSeeSky(pos.above())) {
            return false;
        }

        return level.getBrightness(LightLayer.SKY, pos.above()) >= 14;
    }

    private static boolean isDaytimeSunBlocked(Level level, BlockPos pos) {
        if (!level.isDay()) {
            return false;
        }

        if (level.isRainingAt(pos.above())) {
            return true;
        }

        if (!level.canSeeSky(pos.above())) {
            return true;
        }

        return level.getBrightness(LightLayer.SKY, pos.above()) < 14;
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

                    if (block == net.minecraft.world.level.block.Blocks.FIRE
                            || block == net.minecraft.world.level.block.Blocks.SOUL_FIRE
                            || block == net.minecraft.world.level.block.Blocks.CAMPFIRE
                            || block == net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE) {
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

    private static boolean isIndirectCampfireHeat(Level level, BlockPos rackPos, BlockPos heatPos) {
        BlockState state = level.getBlockState(heatPos);

        if (!(state.getBlock() instanceof CampfireBlock)) {
            return false;
        }

        if (!state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT)) {
            return false;
        }

        int dx = Integer.signum(rackPos.getX() - heatPos.getX());
        int dz = Integer.signum(rackPos.getZ() - heatPos.getZ());

        if (dx == 0 && dz == 0) {
            return false;
        }

        BlockPos between = heatPos.offset(dx, 0, dz);
        BlockState betweenState = level.getBlockState(between);

        return betweenState.isFaceSturdy(level, between, Direction.UP)
                || betweenState.isFaceSturdy(level, between, Direction.NORTH)
                || betweenState.isFaceSturdy(level, between, Direction.SOUTH)
                || betweenState.isFaceSturdy(level, between, Direction.EAST)
                || betweenState.isFaceSturdy(level, between, Direction.WEST);
    }

    private static boolean isFlueHeatSource(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == com.diggydwarff.tobacconistmod.block.ModBlocks.FLUE_FIREBOX.get()) {
            return state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);
        }

        return false;
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!storedLeaf.isEmpty()) {
            tag.put("StoredLeaf", storedLeaf.save(new CompoundTag()));
        }

        tag.putInt("DryingProgress", dryingProgress);
        tag.putInt("SunExposureTicks", sunExposureTicks);
        tag.putInt("InterruptionCount", interruptionCount);
        tag.putBoolean("LastTickHadValidDrying", lastTickHadValidDrying);
        tag.putBoolean("UsedFireDrying", usedFireDrying);
        tag.putBoolean("UsedFlueDrying", usedFlueDrying);
        tag.putInt("AirTicks", airTicks);
        tag.putInt("SunTicks", sunTicks);
        tag.putInt("FireTicks", fireTicks);
        tag.putInt("FlueTicks", flueTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("StoredLeaf")) {
            storedLeaf = ItemStack.of(tag.getCompound("StoredLeaf"));
        } else {
            storedLeaf = ItemStack.EMPTY;
        }

        dryingProgress = tag.getInt("DryingProgress");
        sunExposureTicks = tag.getInt("SunExposureTicks");
        interruptionCount = tag.getInt("InterruptionCount");
        lastTickHadValidDrying = tag.getBoolean("LastTickHadValidDrying");
        usedFireDrying = tag.getBoolean("UsedFireDrying");
        usedFlueDrying = tag.getBoolean("UsedFlueDrying");
        airTicks = tag.getInt("AirTicks");
        sunTicks = tag.getInt("SunTicks");
        fireTicks = tag.getInt("FireTicks");
        flueTicks = tag.getInt("FlueTicks");
    }
}