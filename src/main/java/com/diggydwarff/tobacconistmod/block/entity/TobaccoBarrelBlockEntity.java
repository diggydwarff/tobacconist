package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoSpecialProcessingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TobaccoBarrelBlockEntity extends BlockEntity {

    public static final String TAG_FERMENTED = "Fermented";
    public static final String TAG_AGED_DAYS = "AgedDays";
    public static final String TAG_LAST_SPOIL_CHECK_MONTH = "LastSpoilCheckMonth";
    public static final String TAG_RUINED = "Ruined";

    public static final String TAG_LAST_AGE_GAME_TIME = "LastAgeGameTime";
    public static final String TAG_LAST_FERMENT_GAME_TIME = "LastFermentGameTime";
    private static final String TAG_MONOTONIC_PROCESS_CLOCK = "MonotonicProcessClock";

    public static final int MAX_STACK = 64;

    private static final int TICKS_PER_DAY = 24000;
    private static final int FERMENT_TIME = 48000;
    private static final int PRESS_TIME = 12000;
    private static final int STOVE_TIME = 36000;
    private static final int MAX_BARREL_HUMIDITY = 100;
    private static final int MIN_FERMENT_HUMIDITY = 25;

    private static final int OVERHEAT_THRESHOLD = 7;
    private static final int OVERHEAT_RUIN_TICKS = 6000;

    private ItemStack storedTobacco = ItemStack.EMPTY;

    private int processTicks = 0;
    private int barrelHumidity = 0;
    private int overheatTicks = 0;

    private long lastAgeGameTime = -1L;
    private long lastFermentGameTime = -1L;
    private boolean monotonicProcessClock = true;

    private TobaccoBarrelMode mode = TobaccoBarrelMode.IDLE;
    private final IItemHandler itemHandler = new TobaccoBarrelItemHandler(this);

    public TobaccoBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOBACCO_BARREL.get(), pos, state);
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TobaccoBarrelBlockEntity barrel) {
        if (barrel.storedTobacco.isEmpty()) {
            barrel.mode = TobaccoBarrelMode.IDLE;
            barrel.processTicks = 0;
            barrel.barrelHumidity = 0;
            barrel.overheatTicks = 0;
            barrel.lastAgeGameTime = -1L;
            barrel.lastFermentGameTime = -1L;
            barrel.setChanged();
            if (level.getGameTime() % 20 == 0) barrel.syncToClient();
            return;
        }

        int humidityEnv = BarrelEnvironmentHelper.getHumidity(level, pos);
        int warmth = BarrelEnvironmentHelper.getWarmth(level, pos);

        barrel.updateBarrelHumidity(humidityEnv, warmth);
        barrel.updateOverheat(warmth);

        if (isRuined(barrel.storedTobacco)) {
            barrel.mode = TobaccoBarrelMode.IDLE;
            barrel.processTicks = 0;
            barrel.lastAgeGameTime = -1L;
            barrel.lastFermentGameTime = -1L;
            barrel.setChanged();
            if (level.getGameTime() % 20 == 0) barrel.syncToClient();
            return;
        }

        TobaccoBarrelMode newMode = TobaccoBarrelMode.IDLE;

        if (barrel.canPressPlug()) {
            // Pressing loose Rough tobacco into Plug is a physical operation of its own. It does
            // not require fermentation or aging conditions and does not mark the tobacco as either.
            newMode = TobaccoBarrelMode.PRESSING;
        } else if (barrel.canStove()) {
            newMode = TobaccoBarrelMode.STOVING;
        } else if (barrel.canFerment(warmth)) {
            // Pressure Fermenting is reserved for processes where pressure is actually part of
            // the recipe (Perique/Cavendish). An ordinary fermentation batch remains simply
            // Fermenting even if a piston or Mechanical Press happens to be present.
            newMode = barrel.requiresPressureFermentation()
                    ? TobaccoBarrelMode.PRESSURE_FERMENTING
                    : TobaccoBarrelMode.FERMENTING;
        } else if (barrel.canAge(warmth, humidityEnv)) {
            newMode = TobaccoBarrelMode.AGING;
        }

        long now = level.getGameTime();

        // Older v8 saves wrote day-time values into these fields. Day time is mutable via sleep
        // and /time commands, while game time is monotonic. On the first tick after upgrading,
        // rebuild the anchors from the persisted processTicks so existing barrels keep their
        // visible progress without inheriting a bogus/negative elapsed interval.
        if (!barrel.monotonicProcessClock) {
            barrel.migrateProcessingClock(now);
        }

        if (newMode != barrel.mode) {
            boolean fermentationLabelOnlyChange = isFermentationMode(newMode)
                    && isFermentationMode(barrel.mode);
            barrel.mode = newMode;

            // Toggling incidental pressure during an ordinary fermentation batch changes only the
            // displayed mode; it must not throw away already-earned fermentation time.
            if (!fermentationLabelOnlyChange) {
                barrel.processTicks = 0;
            }

            if (isFiniteFermentationOrProcessingMode(newMode)) {
                barrel.lastAgeGameTime = -1L;
                if (!fermentationLabelOnlyChange || barrel.lastFermentGameTime < 0L) {
                    barrel.lastFermentGameTime = now;
                }
            } else if (newMode == TobaccoBarrelMode.AGING) {
                barrel.lastFermentGameTime = -1L;
                barrel.lastAgeGameTime = now;
            } else {
                barrel.lastAgeGameTime = -1L;
                barrel.lastFermentGameTime = -1L;
            }
        }

        if (barrel.mode == TobaccoBarrelMode.IDLE) {
            barrel.setChanged();
            if (level.getGameTime() % 20 == 0) barrel.syncToClient();
            return;
        }

        if (isFiniteFermentationOrProcessingMode(barrel.mode)) {
            if (barrel.lastFermentGameTime < 0L) {
                barrel.lastFermentGameTime = now;
            }

            int required = barrel.getFiniteProcessTime();
            long elapsed = Math.max(0L, now - barrel.lastFermentGameTime);
            barrel.processTicks = (int) Math.min(elapsed, required);

            if (elapsed >= required) {
                if (barrel.mode == TobaccoBarrelMode.PRESSING) barrel.finishPressing();
                else if (barrel.mode == TobaccoBarrelMode.STOVING) barrel.finishStoving();
                else barrel.finishFermentation();
                barrel.lastFermentGameTime = -1L;
                barrel.processTicks = 0;
                barrel.mode = TobaccoBarrelMode.IDLE;
            }
        } else if (barrel.mode == TobaccoBarrelMode.AGING) {
            if (barrel.lastAgeGameTime < 0L) {
                barrel.lastAgeGameTime = now;
            }

            long elapsed = Math.max(0L, now - barrel.lastAgeGameTime);
            int daysPassed = (int) (elapsed / TICKS_PER_DAY);

            if (daysPassed > 0) {
                for (int i = 0; i < daysPassed; i++) {
                    barrel.advanceAgingDay();
                }
                barrel.lastAgeGameTime += (long) daysPassed * TICKS_PER_DAY;
            }

            barrel.processTicks = (int) (elapsed % TICKS_PER_DAY);
        }

        barrel.setChanged();
        if (level.getGameTime() % 20 == 0) barrel.syncToClient();
    }

    private void migrateProcessingClock(long now) {
        if (isFiniteFermentationOrProcessingMode(mode)) {
            int required = getFiniteProcessTime();
            int preserved = Math.max(0, Math.min(processTicks, required));
            lastFermentGameTime = now - preserved;
            lastAgeGameTime = -1L;
        } else if (mode == TobaccoBarrelMode.AGING) {
            int preserved = Math.max(0, Math.min(processTicks, TICKS_PER_DAY - 1));
            lastAgeGameTime = now - preserved;
            lastFermentGameTime = -1L;
        } else {
            lastAgeGameTime = -1L;
            lastFermentGameTime = -1L;
        }
        monotonicProcessClock = true;
        setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TobaccoBarrelBlockEntity barrel) {
        // Barrel processing is intentionally visually sealed. Fermentation, pressure fermentation,
        // pressing, stoving, and aging do not emit smoke particles from the barrel itself. Visible
        // smoke belongs to curing/fire sources (including Haunted soul-fire curing), not storage.
    }

    private void updateBarrelHumidity(int humidityEnv, int warmth) {
        int delta = humidityEnv + 1;

        if (warmth >= 3) {
            delta += 1;
        }

        barrelHumidity = Math.min(MAX_BARREL_HUMIDITY, barrelHumidity + delta);

        if (humidityEnv <= -1) {
            barrelHumidity = Math.max(0, barrelHumidity - 1);
        }
    }

    private void updateOverheat(int warmth) {
        if (warmth >= OVERHEAT_THRESHOLD) {
            overheatTicks++;
        } else {
            overheatTicks = Math.max(0, overheatTicks - 2);
        }

        if (overheatTicks >= OVERHEAT_RUIN_TICKS) {
            spoilStoredTobacco(25);
        }
    }

    private boolean canPressPlug() {
        return TobaccoProcessingHelper.canMechanicallyPressToPlug(storedTobacco) && hasPressureWeight();
    }

    private boolean canStove() {
        return TobaccoSpecialProcessingHelper.isStovedVirginiaCandidate(storedTobacco)
                && hasStovingHeat();
    }

    private boolean hasStovingHeat() {
        return level != null && BarrelEnvironmentHelper.hasNearbyLitFlueFirebox(level, worldPosition);
    }

    private boolean canFerment(int warmth) {
        if (storedTobacco.isEmpty()) return false;
        if (isRuined(storedTobacco)) return false;

        boolean conditions = warmth >= 3 && barrelHumidity >= MIN_FERMENT_HUMIDITY;
        if (!conditions) return false;

        // Perique is specifically pressure-fermented. Do not let an intact air-cured Burley
        // batch become generic fermented tobacco first and permanently miss the Perique path.
        if (TobaccoSpecialProcessingHelper.isPeriqueCandidate(storedTobacco)) {
            return hasPressureWeight();
        }

        // Cavendish is pressure-fermented. Black Cavendish is a deliberate second, heated pressure cycle.
        if (TobaccoSpecialProcessingHelper.isBlackCavendishCandidate(storedTobacco)) {
            return hasPressureWeight() && hasStovingHeat();
        }
        if (TobaccoSpecialProcessingHelper.isCavendishCandidate(storedTobacco)) {
            // Cavendish needs the pressed plug to be heated/steamed as well as kept under
            // pressure. This prevents an ordinary Plug left under generic pressure from silently
            // becoming Cavendish in any generically warm fermentation room.
            return hasPressureWeight() && hasStovingHeat();
        }
        if (isFermented(storedTobacco)) return false;
        return true;
    }

    /** Returns true only when pressure is a required part of the active fermentation recipe. */
    private boolean requiresPressureFermentation() {
        return TobaccoSpecialProcessingHelper.isPeriqueCandidate(storedTobacco)
                || TobaccoSpecialProcessingHelper.isCavendishCandidate(storedTobacco)
                || TobaccoSpecialProcessingHelper.isBlackCavendishCandidate(storedTobacco);
    }

    private boolean hasPressureWeight() {
        if (level == null) return false;

        // Vanilla pressure: a downward-facing piston sits two blocks above the barrel. When
        // powered, its extended head occupies the block directly above the barrel and acts as
        // the press. Requiring the actual extended head prevents an unpowered piston from
        // supplying free pressure.
        BlockState head = level.getBlockState(worldPosition.above());
        BlockState base = level.getBlockState(worldPosition.above(2));
        boolean vanillaPistonPressure = head.is(Blocks.PISTON_HEAD)
                && head.hasProperty(BlockStateProperties.FACING)
                && head.getValue(BlockStateProperties.FACING) == Direction.DOWN
                && (base.is(Blocks.PISTON) || base.is(Blocks.STICKY_PISTON))
                && base.hasProperty(BlockStateProperties.FACING)
                && base.getValue(BlockStateProperties.FACING) == Direction.DOWN
                && base.hasProperty(BlockStateProperties.EXTENDED)
                && base.getValue(BlockStateProperties.EXTENDED);
        if (vanillaPistonPressure) return true;

        // Create presses normally sit two blocks above the processing surface (one block of
        // working clearance). Also accept the adjacent position for compact/custom setups.
        return com.diggydwarff.tobacconistmod.compat.create.CreateCompat.isBarrelPressureSource(
                level, worldPosition.above())
                || com.diggydwarff.tobacconistmod.compat.create.CreateCompat.isBarrelPressureSource(
                level, worldPosition.above(2));
    }

    private static boolean isFermentationMode(TobaccoBarrelMode mode) {
        return mode == TobaccoBarrelMode.FERMENTING
                || mode == TobaccoBarrelMode.PRESSURE_FERMENTING;
    }

    private static boolean isFiniteFermentationOrProcessingMode(TobaccoBarrelMode mode) {
        return isFermentationMode(mode)
                || mode == TobaccoBarrelMode.PRESSING
                || mode == TobaccoBarrelMode.STOVING;
    }

    private int getFiniteProcessTime() {
        return switch (mode) {
            case PRESSING -> PRESS_TIME;
            case STOVING -> STOVE_TIME;
            default -> FERMENT_TIME;
        };
    }

    private boolean canAge(int warmth, int humidity) {
        if (storedTobacco.isEmpty()) return false;
        if (isRuined(storedTobacco)) return false;
        if (level == null) return false;

        return warmth <= 0
                && humidity >= 1 && humidity <= 3
                && BarrelEnvironmentHelper.isCoolDarkStorage(level, worldPosition);
    }

    private void finishPressing() {
        ItemStack pressed = TobaccoProcessingHelper.mechanicallyPressOne(storedTobacco);
        if (!pressed.isEmpty()) {
            pressed.setCount(storedTobacco.getCount());
            storedTobacco = pressed;
        }
    }

    private void finishStoving() {
        TobaccoSpecialProcessingHelper.applyNamedCure(storedTobacco, TobaccoCuringHelper.CURE_STOVED_VIRGINIA);
    }

    private void finishFermentation() {
        processTicks = 0;
        CompoundTag tag = LegacyItemTags.getOrCreateTag(storedTobacco);

        String targetCure = "";
        int qualityBonus = 7;
        if (TobaccoSpecialProcessingHelper.isBlackCavendishCandidate(storedTobacco)
                && hasPressureWeight() && hasStovingHeat()) {
            targetCure = TobaccoCuringHelper.CURE_BLACK_CAVENDISH;
            qualityBonus = 3;
        } else if (TobaccoSpecialProcessingHelper.isPeriqueCandidate(storedTobacco) && hasPressureWeight()) {
            targetCure = TobaccoCuringHelper.CURE_PERIQUE;
        } else if (TobaccoSpecialProcessingHelper.isCavendishCandidate(storedTobacco)) {
            targetCure = TobaccoCuringHelper.CURE_CAVENDISH;
        }

        if (!targetCure.isEmpty()) {
            tag.putString(TobaccoCuringHelper.TAG_CURE_TYPE, targetCure);
        }
        tag.putBoolean(TAG_FERMENTED, true);

        int q = TobaccoCuringHelper.getQuality(storedTobacco);
        int newQ = Math.min(120, q + qualityBonus);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(newQ));
    }

    private void advanceAgingDay() {
        CompoundTag tag = LegacyItemTags.getOrCreateTag(storedTobacco);

        int agedDays = tag.getInt(TAG_AGED_DAYS) + 1;
        tag.putInt(TAG_AGED_DAYS, agedDays);

        applyAgingQualityBonus(tag, agedDays);
        trySpoilFromExtremeAge(tag, agedDays);
    }

    private void applyAgingQualityBonus(CompoundTag tag, int agedDays) {
        int q = TobaccoCuringHelper.getQuality(storedTobacco);
        int bonus = 0;

        if (agedDays <= 7) {
            if (agedDays % 3 == 0) bonus = 1;
        } else if (agedDays <= 30) {
            if (agedDays % 7 == 0) bonus = 1;
        } else if (agedDays <= 90) {
            if (agedDays % 15 == 0) bonus = 1;
        } else if (agedDays <= 365) {
            if (agedDays % 30 == 0) bonus = 1;
        }

        if (bonus > 0) {
            int newQ = Math.min(120, q + bonus);
            tag.putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
            tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(newQ));
        }
    }

    private void trySpoilFromExtremeAge(CompoundTag tag, int agedDays) {
        if (level == null) return;
        if (agedDays <= 365) return;

        int monthIndex = (agedDays - 366) / 30;
        int lastCheckedMonth = tag.getInt(TAG_LAST_SPOIL_CHECK_MONTH);

        if (monthIndex <= lastCheckedMonth) {
            return;
        }

        tag.putInt(TAG_LAST_SPOIL_CHECK_MONTH, monthIndex);

        double spoilChance = Math.min(0.10, 0.005 * monthIndex);

        if (level.random.nextDouble() < spoilChance) {
            spoilStoredTobacco(15);
        }
    }

    private void spoilStoredTobacco(int qualityPenalty) {
        if (storedTobacco.isEmpty()) return;

        int count = storedTobacco.getCount();
        int q = TobaccoCuringHelper.getQuality(storedTobacco);
        int ruinedQuality = Math.max(0, q - qualityPenalty);

        ItemStack spoiled = new ItemStack(ModItems.SPOILED_TOBACCO.get(), count);

        if (LegacyItemTags.hasTag(storedTobacco)) {
            LegacyItemTags.setTag(spoiled, LegacyItemTags.getTag(storedTobacco).copy());
        }

        CompoundTag tag = LegacyItemTags.getOrCreateTag(spoiled);
        tag.putBoolean(TAG_RUINED, true);
        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, ruinedQuality);
        tag.putString(
                TobaccoCuringHelper.TAG_QUALITY_TIER,
                TobaccoCuringHelper.getQualityTierId(ruinedQuality)
        );

        storedTobacco = spoiled;
        mode = TobaccoBarrelMode.IDLE;
        processTicks = 0;
        lastAgeGameTime = -1L;
        lastFermentGameTime = -1L;
    }

    public int tryInsertTobacco(ItemStack stack) {
        return insertTobacco(stack, true);
    }

    public int tryInsertTobaccoAutomated(ItemStack stack) {
        return insertTobacco(stack, false);
    }

    public int getInsertableAmount(ItemStack stack) {
        if (stack.isEmpty() || !isValidTobacco(stack)) return 0;

        if (storedTobacco.isEmpty()) {
            return Math.min(stack.getCount(), MAX_STACK);
        }

        if (!ItemStack.isSameItemSameTags(storedTobacco, stack)) {
            return 0;
        }

        return Math.min(stack.getCount(), Math.max(0, MAX_STACK - storedTobacco.getCount()));
    }

    private int insertTobacco(ItemStack stack, boolean playSound) {
        int move = getInsertableAmount(stack);
        if (move <= 0) return 0;

        if (storedTobacco.isEmpty()) {
            storedTobacco = stack.copyWithCount(move);
            processTicks = 0;
            overheatTicks = 0;
            mode = TobaccoBarrelMode.IDLE;
            lastAgeGameTime = -1L;
            lastFermentGameTime = -1L;
        } else {
            storedTobacco.grow(move);
            lastAgeGameTime = -1L;
            lastFermentGameTime = -1L;
            processTicks = 0;
            mode = TobaccoBarrelMode.IDLE;
        }

        if (playSound) {
            playBarrelSound(SoundEvents.BARREL_OPEN);
        }
        setChanged();
        syncToClient();
        return move;
    }

    public ItemStack extractTobaccoAutomated(int amount, boolean simulate) {
        if (amount <= 0 || storedTobacco.isEmpty() || isAutomatedExtractionLocked()) {
            return ItemStack.EMPTY;
        }

        int move = Math.min(amount, storedTobacco.getCount());
        if (simulate) {
            return storedTobacco.copyWithCount(move);
        }

        ItemStack out = storedTobacco.split(move);
        if (storedTobacco.isEmpty()) {
            storedTobacco = ItemStack.EMPTY;
            barrelHumidity = 0;
            overheatTicks = 0;
        }

        // Disturbing a batch discards only its partial current-cycle progress. Metadata already
        // earned by fermentation/aging remains on both the extracted and remaining stacks.
        processTicks = 0;
        mode = TobaccoBarrelMode.IDLE;
        lastAgeGameTime = -1L;
        lastFermentGameTime = -1L;
        setChanged();
        syncToClient();
        return out;
    }

    public ItemStack getStoredTobaccoCopy() {
        return storedTobacco.copy();
    }

    public ItemStack removeStoredTobacco() {
        if (storedTobacco.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack out = storedTobacco.copy();
        storedTobacco = ItemStack.EMPTY;
        processTicks = 0;
        barrelHumidity = 0;
        overheatTicks = 0;
        mode = TobaccoBarrelMode.IDLE;
        lastAgeGameTime = -1L;
        lastFermentGameTime = -1L;
        playBarrelSound(SoundEvents.BARREL_CLOSE);
        setChanged();
        return out;
    }

    private void playBarrelSound(net.minecraft.sounds.SoundEvent soundEvent) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, soundEvent, SoundSource.BLOCKS, 0.65F, 1.0F);
        }
    }

    private boolean isValidTobacco(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() == ModItems.SPOILED_TOBACCO.get()) return false;

        if (TobaccoCuringHelper.isLooseTobacco(stack)) {
            return true;
        }

        if (stack.getItem() instanceof com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoLeafItem) {
            return LegacyItemTags.hasTag(stack) && LegacyItemTags.getTag(stack).contains(TobaccoCuringHelper.TAG_CURE_TYPE);
        }

        return false;
    }

    public Component[] getStatusMessage() {
        Component itemName = storedTobacco.isEmpty()
                ? Component.translatable("tobacconistmod.ui.empty")
                : Component.translatable("tobacconistmod.ui.item_count", storedTobacco.getHoverName(), storedTobacco.getCount());

        int warmth = level != null ? BarrelEnvironmentHelper.getWarmth(level, worldPosition) : 0;
        int humidity = level != null ? BarrelEnvironmentHelper.getHumidity(level, worldPosition) : 0;
        int blockLight = level != null ? level.getBrightness(LightLayer.BLOCK, worldPosition.above()) : 0;
        boolean coolDark = level != null && BarrelEnvironmentHelper.isCoolDarkStorage(level, worldPosition);
        int agedDays = getAgedDays(storedTobacco);

        Component line1 = Component.translatable(
                "tobacconistmod.barrel.status.line1",
                itemName,
                TobaccoText.barrelMode(mode).withStyle(ChatFormatting.GOLD)
        );

        MutableComponent line2 = Component.translatable(
                "tobacconistmod.barrel.status.line2",
                warmth,
                humidity,
                barrelHumidity,
                blockLight,
                Component.translatable(coolDark ? "tobacconistmod.ui.yes" : "tobacconistmod.ui.no"),
                TobaccoText.ageDuration(agedDays),
                TobaccoText.ageLabel(agedDays)
        );

        if (isFermentationMode(mode)) {
            double pct = Math.min(100.0, processTicks * 100.0 / FERMENT_TIME);
            line2.append(Component.translatable("tobacconistmod.barrel.status.ferment_progress", String.format("%.1f", pct)));
        } else if (mode == TobaccoBarrelMode.PRESSING || mode == TobaccoBarrelMode.STOVING) {
            double pct = Math.min(100.0, processTicks * 100.0 / getFiniteProcessTime());
            line2.append(Component.translatable("tobacconistmod.barrel.status.process_progress", String.format("%.1f", pct)));
        } else if (mode == TobaccoBarrelMode.AGING) {
            double pct = Math.min(100.0, processTicks * 100.0 / TICKS_PER_DAY);
            line2.append(Component.translatable("tobacconistmod.barrel.status.aging_progress", String.format("%.1f", pct)));
        }

        return new Component[]{line1, line2};
    }

    public MutableComponent getModeComponent() {
        return TobaccoText.barrelMode(mode);
    }

    public String getModeNameForInspection() {
        return getModeComponent().getString();
    }

    public TobaccoBarrelMode getMode() {
        return mode;
    }

    /**
     * Fermentation (ordinary or under pressure) is a finite protected batch process.
     * Generic automation must not pull from the barrel until it completes. Aging remains extractable so Create
     * Attribute Filters can release tobacco at a player-selected age threshold.
     */
    public boolean isAutomatedExtractionLocked() {
        return isFermentationMode(mode) || mode == TobaccoBarrelMode.PRESSING || mode == TobaccoBarrelMode.STOVING;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getBarrelHumidity() {
        return barrelHumidity;
    }

    public ItemStack getStoredTobacco() {
        return storedTobacco;
    }

    public static boolean isFermented(ItemStack stack) {
        return LegacyItemTags.hasTag(stack) && LegacyItemTags.getTag(stack).getBoolean(TAG_FERMENTED);
    }

    public static int getAgedDays(ItemStack stack) {
        return LegacyItemTags.hasTag(stack) ? LegacyItemTags.getTag(stack).getInt(TAG_AGED_DAYS) : 0;
    }

    public static boolean isRuined(ItemStack stack) {
        return LegacyItemTags.hasTag(stack) && LegacyItemTags.getTag(stack).getBoolean(TAG_RUINED);
    }

    public int getProcessProgressPercent() {
        return switch (mode) {
            case FERMENTING, PRESSURE_FERMENTING -> Math.min(100, (processTicks * 100) / FERMENT_TIME);
            case PRESSING -> Math.min(100, (processTicks * 100) / PRESS_TIME);
            case STOVING -> Math.min(100, (processTicks * 100) / STOVE_TIME);
            case AGING -> Math.min(100, (processTicks * 100) / TICKS_PER_DAY);
            default -> 0;
        };
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!storedTobacco.isEmpty()) {
            tag.put("StoredTobacco", storedTobacco.save(new CompoundTag()));
        }

        tag.putInt("ProcessTicks", processTicks);
        tag.putInt("BarrelHumidity", barrelHumidity);
        tag.putInt("OverheatTicks", overheatTicks);
        tag.putString("Mode", mode.name());
        tag.putLong(TAG_LAST_AGE_GAME_TIME, lastAgeGameTime);
        tag.putLong(TAG_LAST_FERMENT_GAME_TIME, lastFermentGameTime);
        tag.putBoolean(TAG_MONOTONIC_PROCESS_CLOCK, monotonicProcessClock);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("StoredTobacco")) {
            storedTobacco = ItemStack.of(tag.getCompound("StoredTobacco"));
        } else {
            storedTobacco = ItemStack.EMPTY;
        }

        processTicks = tag.getInt("ProcessTicks");
        barrelHumidity = tag.getInt("BarrelHumidity");
        overheatTicks = tag.getInt("OverheatTicks");
        lastAgeGameTime = tag.contains(TAG_LAST_AGE_GAME_TIME) ? tag.getLong(TAG_LAST_AGE_GAME_TIME) : -1L;
        lastFermentGameTime = tag.contains(TAG_LAST_FERMENT_GAME_TIME) ? tag.getLong(TAG_LAST_FERMENT_GAME_TIME) : -1L;
        monotonicProcessClock = tag.getBoolean(TAG_MONOTONIC_PROCESS_CLOCK);

        try {
            mode = TobaccoBarrelMode.valueOf(tag.getString("Mode"));
        } catch (Exception ignored) {
            mode = TobaccoBarrelMode.IDLE;
        }
    }

    public List<Component> getFullDebugLines() {
        Component itemName = storedTobacco.isEmpty()
                ? Component.translatable("tobacconistmod.ui.empty")
                : Component.translatable("tobacconistmod.ui.item_count", storedTobacco.getHoverName(), storedTobacco.getCount());

        int warmth = level != null ? BarrelEnvironmentHelper.getWarmth(level, worldPosition) : 0;
        int humidity = level != null ? BarrelEnvironmentHelper.getHumidity(level, worldPosition) : 0;
        int blockLight = level != null ? level.getBrightness(LightLayer.BLOCK, worldPosition.above()) : 0;
        boolean coolDark = level != null && BarrelEnvironmentHelper.isCoolDarkStorage(level, worldPosition);
        int agedDays = getAgedDays(storedTobacco);

        Component progress = Component.translatable("tobacconistmod.ui.none");
        if (isFermentationMode(mode)) {
            double pct = Math.min(100.0, processTicks * 100.0 / FERMENT_TIME);
            progress = Component.translatable("tobacconistmod.debug.ferment_progress", String.format("%.1f", pct));
        } else if (mode == TobaccoBarrelMode.PRESSING || mode == TobaccoBarrelMode.STOVING) {
            double pct = Math.min(100.0, processTicks * 100.0 / getFiniteProcessTime());
            progress = Component.translatable("tobacconistmod.debug.process_progress", String.format("%.1f", pct));
        } else if (mode == TobaccoBarrelMode.AGING) {
            double pct = Math.min(100.0, processTicks * 100.0 / TICKS_PER_DAY);
            progress = Component.translatable("tobacconistmod.debug.aging_progress", String.format("%.1f", pct));
        }

        return List.of(
                Component.translatable("tobacconistmod.debug.tobacco_barrel_title").withStyle(ChatFormatting.GOLD),
                Component.translatable("tobacconistmod.debug.stored", itemName),
                Component.translatable("tobacconistmod.debug.mode", getModeComponent()),
                Component.translatable("tobacconistmod.debug.warmth", warmth),
                Component.translatable("tobacconistmod.debug.humidity", humidity),
                Component.translatable("tobacconistmod.debug.barrel_humidity", barrelHumidity),
                Component.translatable("tobacconistmod.debug.light", blockLight),
                Component.translatable("tobacconistmod.debug.cool_dark", TobaccoText.yesNo(coolDark)),
                Component.translatable("tobacconistmod.debug.age", TobaccoText.ageDuration(agedDays)),
                progress,
                Component.translatable("tobacconistmod.debug.overheat_ticks", overheatTicks),
                Component.translatable("tobacconistmod.debug.ruined", TobaccoText.yesNo(isRuined(storedTobacco))),
                Component.translatable("tobacconistmod.debug.fermented", TobaccoText.yesNo(isFermented(storedTobacco)))
        );
    }


    /** QA helper: immediately completes a valid Rough Cut -> Pressed Plug operation. */
    public boolean forceFinishPressing() {
        if (storedTobacco.isEmpty()) return false;
        if (isRuined(storedTobacco)) return false;
        if (!TobaccoProcessingHelper.canMechanicallyPressToPlug(storedTobacco)) return false;

        finishPressing();
        processTicks = 0;
        mode = TobaccoBarrelMode.IDLE;
        lastFermentGameTime = -1L;
        setChanged();
        syncToClient();
        return true;
    }

    /** QA helper: immediately completes whichever finite barrel process is currently active. */
    public boolean forceFinishCurrentProcess() {
        if (storedTobacco.isEmpty() || isRuined(storedTobacco)) return false;

        switch (mode) {
            case PRESSING -> finishPressing();
            case FERMENTING, PRESSURE_FERMENTING -> finishFermentation();
            case STOVING -> finishStoving();
            default -> {
                return false;
            }
        }

        processTicks = 0;
        mode = TobaccoBarrelMode.IDLE;
        lastAgeGameTime = -1L;
        lastFermentGameTime = -1L;
        setChanged();
        syncToClient();
        return true;
    }

    public void forceFinishFermentation() {
        if (storedTobacco.isEmpty()) return;
        if (isRuined(storedTobacco)) return;

        // Cavendish is already marked fermented, so allow the debug helper to advance it
        // through the deliberate second fermentation into Black Cavendish.
        if (TobaccoSpecialProcessingHelper.isBlackCavendishCandidate(storedTobacco)) {
            CompoundTag tag = LegacyItemTags.getOrCreateTag(storedTobacco);
            tag.putString(TobaccoCuringHelper.TAG_CURE_TYPE, TobaccoCuringHelper.CURE_BLACK_CAVENDISH);
            tag.putBoolean(TAG_FERMENTED, true);
            int q = TobaccoCuringHelper.getQuality(storedTobacco);
            int newQ = Math.min(120, q + 3);
            tag.putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
            tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(newQ));
        } else {
            if (isFermented(storedTobacco)) return;
            finishFermentation();
        }

        processTicks = 0;
        mode = TobaccoBarrelMode.IDLE;
        lastFermentGameTime = -1L;
        setChanged();
        syncToClient();
    }

    public void addAgedDays(int days) {
        if (storedTobacco.isEmpty()) return;
        if (days <= 0) return;

        for (int i = 0; i < days; i++) {
            advanceAgingDay();
        }

        processTicks = 0;
        mode = TobaccoBarrelMode.IDLE;
        lastAgeGameTime = -1L;
        setChanged();
    }

    public void forceRuin() {
        if (storedTobacco.isEmpty()) return;

        spoilStoredTobacco(25);
        setChanged();
    }
}