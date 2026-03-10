package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TobaccoDryingRackBlockEntity extends BlockEntity {

    public static final int MAX_LEAVES = 16;
    public static final int AIR_DRY_TIME = 20 * 60 * 4;   // 4 min
    public static final int FIRE_DRY_TIME = 20 * 60 * 2;  // 2 min
    public static final int SUN_REQUIRED_TICKS = 20 * 60; // 1 min sunlight exposure

    private ItemStack storedLeaf = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private int sunExposureTicks = 0;
    private int interruptionCount = 0;
    private boolean lastTickHadValidDrying = false;
    private boolean usedFireDrying = false;

    private int airTicks = 0;
    private int sunTicks = 0;
    private int fireTicks = 0;

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
            lastTickHadValidDrying = false;
            airTicks = 0;
            sunTicks = 0;
            fireTicks = 0;
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

        int needed = usedFireDrying ? FIRE_DRY_TIME : AIR_DRY_TIME;
        if (needed <= 0) {
            return 0;
        }

        return Math.min(100, (dryingProgress * 100) / needed);
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
        return usedFireDrying ? FIRE_DRY_TIME : AIR_DRY_TIME;
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

        return isOverLitCampfire(level, worldPosition) || canAirDry(level, worldPosition);
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
        lastTickHadValidDrying = false;
        airTicks = 0;
        sunTicks = 0;
        fireTicks = 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TobaccoDryingRackBlockEntity rack) {
        if (level.isClientSide) {
            return;
        }

        boolean overCampfire = isOverLitCampfire(level, pos);
        boolean canAirDry = canAirDry(level, pos);
        boolean inSun = hasDirectSunlight(level, pos);

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

        boolean validDryingThisTick = overCampfire || canAirDry;

        if (!validDryingThisTick && rack.lastTickHadValidDrying) {
            rack.interruptionCount++;
            rack.syncToClient();
        }
        rack.lastTickHadValidDrying = validDryingThisTick;

        if (!validDryingThisTick) {
            return;
        }

        if (overCampfire) {
            rack.usedFireDrying = true;
        }

        rack.dryingProgress++;

        if (overCampfire) {
            rack.fireTicks++;
        } else if (inSun) {
            rack.sunTicks++;
            rack.sunExposureTicks++;
        } else {
            rack.airTicks++;
        }

        int needed = overCampfire ? FIRE_DRY_TIME : AIR_DRY_TIME;

        if (rack.dryingProgress >= needed) {
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

        int dominant = Math.max(fireTicks, Math.max(sunTicks, airTicks));

        String cureType;
        if (dominant == fireTicks) {
            cureType = TobaccoCuringHelper.CURE_FIRE;
        } else if (dominant == sunTicks) {
            cureType = TobaccoCuringHelper.CURE_SUN;
        } else {
            cureType = TobaccoCuringHelper.CURE_AIR;
        }

        int total = fireTicks + sunTicks + airTicks;
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
        airTicks = 0;
        sunTicks = 0;
        fireTicks = 0;

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
        if (!canAirDry(level, pos)) {
            return false;
        }

        if (!level.isDay()) {
            return false;
        }

        return level.canSeeSky(pos.above()) && level.getBrightness(LightLayer.SKY, pos.above()) >= 14;
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
        tag.putInt("AirTicks", airTicks);
        tag.putInt("SunTicks", sunTicks);
        tag.putInt("FireTicks", fireTicks);
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
        airTicks = tag.getInt("AirTicks");
        sunTicks = tag.getInt("SunTicks");
        fireTicks = tag.getInt("FireTicks");
    }
}