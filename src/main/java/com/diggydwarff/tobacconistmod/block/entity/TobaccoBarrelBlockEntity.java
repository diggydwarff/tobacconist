package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TobaccoBarrelBlockEntity extends BlockEntity {

    public static final String TAG_FERMENTED = "Fermented";
    public static final String TAG_AGED_DAYS = "AgedDays";
    public static final String TAG_LAST_SPOIL_CHECK_MONTH = "LastSpoilCheckMonth";
    public static final String TAG_RUINED = "Ruined";

    public static final int MAX_STACK = 16;

    private static final int TICKS_PER_DAY = 24000;
    private static final int TICKS_PER_MONTH = TICKS_PER_DAY * 30;

    private static final int FERMENT_TIME = 48000; // 2 in-game days
    private static final int MAX_BARREL_HUMIDITY = 100;
    private static final int MIN_FERMENT_HUMIDITY = 25;

    private static final int OVERHEAT_THRESHOLD = 7;
    private static final int OVERHEAT_RUIN_TICKS = 6000;

    private ItemStack storedTobacco = ItemStack.EMPTY;

    private int processTicks = 0;
    private int barrelHumidity = 0;
    private int overheatTicks = 0;

    private TobaccoBarrelMode mode = TobaccoBarrelMode.IDLE;

    public TobaccoBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOBACCO_BARREL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TobaccoBarrelBlockEntity barrel) {
        if (barrel.storedTobacco.isEmpty()) {
            barrel.mode = TobaccoBarrelMode.IDLE;
            barrel.processTicks = 0;
            barrel.barrelHumidity = 0;
            barrel.overheatTicks = 0;
            barrel.setChanged();
            return;
        }

        int humidityEnv = BarrelEnvironmentHelper.getHumidity(level, pos);
        int warmth = BarrelEnvironmentHelper.getWarmth(level, pos);

        barrel.updateBarrelHumidity(humidityEnv, warmth);
        barrel.updateOverheat(warmth);

        if (isRuined(barrel.storedTobacco)) {
            barrel.mode = TobaccoBarrelMode.IDLE;
            barrel.processTicks = 0;
            barrel.setChanged();
            return;
        }

        TobaccoBarrelMode newMode = TobaccoBarrelMode.IDLE;

        if (barrel.canFerment(warmth)) {
            newMode = TobaccoBarrelMode.FERMENTING;
        } else if (barrel.canAge(warmth)) {
            newMode = TobaccoBarrelMode.AGING;
        }

        if (newMode != barrel.mode) {
            barrel.mode = newMode;
            barrel.processTicks = 0;
        }

        if (barrel.mode == TobaccoBarrelMode.IDLE) {
            barrel.setChanged();
            return;
        }

        barrel.processTicks++;

        if (barrel.mode == TobaccoBarrelMode.FERMENTING) {
            if (barrel.processTicks >= FERMENT_TIME) {
                barrel.finishFermentation();
            }
        } else if (barrel.mode == TobaccoBarrelMode.AGING) {
            if (barrel.processTicks >= TICKS_PER_DAY) {
                barrel.processTicks = 0;
                barrel.advanceAgingDay();
            }
        }

        barrel.setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TobaccoBarrelBlockEntity barrel) {
        if (barrel.mode == TobaccoBarrelMode.FERMENTING && !barrel.storedTobacco.isEmpty()) {
            if (level.random.nextFloat() < 0.08f) {
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.25;
                double y = pos.getY() + 0.9;
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.25;

                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x, y, z,
                        0.0, 0.01, 0.0
                );
            }
        }
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
            CompoundTag tag = storedTobacco.getOrCreateTag();

            if (!tag.getBoolean(TAG_RUINED)) {
                tag.putBoolean(TAG_RUINED, true);

                int quality = TobaccoCuringHelper.getQuality(storedTobacco);
                int ruinedQuality = Math.max(0, quality - 25);

                tag.putInt(TobaccoCuringHelper.TAG_QUALITY, ruinedQuality);
                tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER,
                        TobaccoCuringHelper.getQualityTierId(ruinedQuality));
            }
        }
    }

    private boolean canFerment(int warmth) {
        if (storedTobacco.isEmpty()) return false;
        if (isRuined(storedTobacco)) return false;
        if (isFermented(storedTobacco)) return false;

        return warmth >= 3 && barrelHumidity >= MIN_FERMENT_HUMIDITY;
    }

    private boolean canAge(int warmth) {
        if (storedTobacco.isEmpty()) return false;
        if (isRuined(storedTobacco)) return false;
        if (level == null) return false;

        int humidity = BarrelEnvironmentHelper.getHumidity(level, worldPosition);

        return warmth <= 0
                && humidity >= 1 && humidity <= 3
                && BarrelEnvironmentHelper.isCoolDarkStorage(level, worldPosition);
    }

    private void finishFermentation() {
        processTicks = 0;

        CompoundTag tag = storedTobacco.getOrCreateTag();
        tag.putBoolean(TAG_FERMENTED, true);

        int q = TobaccoCuringHelper.getQuality(storedTobacco);
        int newQ = Math.min(100, q + 7);

        tag.putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
        tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(newQ));
    }

    private void advanceAgingDay() {
        CompoundTag tag = storedTobacco.getOrCreateTag();

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
            int newQ = Math.min(100, q + bonus);
            tag.putInt(TobaccoCuringHelper.TAG_QUALITY, newQ);
            tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(newQ));
        }
    }

    private void trySpoilFromExtremeAge(CompoundTag tag, int agedDays) {
        if (level == null) return;
        if (agedDays <= 365) return;

        int monthIndex = (agedDays - 365) / 30;
        int lastCheckedMonth = tag.getInt(TAG_LAST_SPOIL_CHECK_MONTH);

        if (monthIndex <= lastCheckedMonth) {
            return;
        }

        tag.putInt(TAG_LAST_SPOIL_CHECK_MONTH, monthIndex);

        double spoilChance = Math.min(0.10, 0.005 * monthIndex);

        if (level.random.nextDouble() < spoilChance) {
            tag.putBoolean(TAG_RUINED, true);

            int q = TobaccoCuringHelper.getQuality(storedTobacco);
            int ruinedQuality = Math.max(0, q - 15);
            tag.putInt(TobaccoCuringHelper.TAG_QUALITY, ruinedQuality);
            tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER,
                    TobaccoCuringHelper.getQualityTierId(ruinedQuality));
        }
    }

    public int tryInsertTobacco(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (!isValidTobacco(stack)) return 0;

        if (storedTobacco.isEmpty()) {
            int move = Math.min(stack.getCount(), MAX_STACK);
            storedTobacco = stack.copyWithCount(move);
            processTicks = 0;
            overheatTicks = 0;
            mode = TobaccoBarrelMode.IDLE;
            setChanged();
            return move;
        }

        if (!ItemStack.isSameItemSameTags(storedTobacco, stack)) {
            return 0;
        }

        if (storedTobacco.getCount() >= MAX_STACK) {
            return 0;
        }

        int space = MAX_STACK - storedTobacco.getCount();
        int move = Math.min(space, stack.getCount());
        storedTobacco.grow(move);
        setChanged();
        return move;
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
        setChanged();
        return out;
    }

    private boolean isValidTobacco(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (TobaccoCuringHelper.isLooseTobacco(stack)) {
            return true;
        }

        if (stack.getItem() instanceof com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoLeafItem) {
            return stack.hasTag() && stack.getTag().contains(TobaccoCuringHelper.TAG_CURE_TYPE);
        }

        return false;
    }

    public Component[] getStatusMessage() {
        String itemName = storedTobacco.isEmpty()
                ? "Empty"
                : storedTobacco.getHoverName().getString() + " x" + storedTobacco.getCount();

        int warmth = level != null ? BarrelEnvironmentHelper.getWarmth(level, worldPosition) : 0;
        int humidity = level != null ? BarrelEnvironmentHelper.getHumidity(level, worldPosition) : 0;
        int blockLight = level != null ? level.getBrightness(LightLayer.BLOCK, worldPosition.above()) : 0;
        boolean coolDark = level != null && BarrelEnvironmentHelper.isCoolDarkStorage(level, worldPosition);

        int agedDays = getAgedDays(storedTobacco);
        int years = agedDays / 365;
        int remDays = agedDays % 365;

        String ageLabel = getAgeDisplayLabel(agedDays);

        String progressText = "";

        if (mode == TobaccoBarrelMode.FERMENTING) {
            progressText = String.format("%.1f%%", processTicks * 100.0 / FERMENT_TIME);
        }

        Component line1 = Component.literal("Tobacco Barrel | Stored: " + itemName + " | Mode: ")
                .append(Component.literal(getModeDisplayName()).withStyle(ChatFormatting.GOLD));

        Component line2 = Component.literal(
                "Warmth: " + warmth +
                        " | Humidity: " + humidity +
                        " | Barrel Humidity: " + barrelHumidity +
                        " | Light: " + blockLight +
                        " | Cool/Dark: " + coolDark +
                        " | Age: " + years + "y " + remDays + "d (" + ageLabel + ")" +
                        (progressText.isEmpty() ? "" : " | Ferment: " + progressText)
        );

        return new Component[]{line1, line2};
    }

    private String getAgeDisplayLabel(int agedDays) {
        if (agedDays < 7) return "Fresh";
        if (agedDays < 30) return "Light Aged";
        if (agedDays < 90) return "Deep Aged";
        if (agedDays < 365) return "Vintage";
        return "Cellared";
    }

    private String getModeDisplayName() {
        return switch (mode) {
            case FERMENTING -> "Fermenting";
            case AGING -> "Aging";
            default -> "Idle";
        };
    }

    public TobaccoBarrelMode getMode() {
        return mode;
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
        return stack.hasTag() && stack.getTag().getBoolean(TAG_FERMENTED);
    }

    public static int getAgedDays(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(TAG_AGED_DAYS) : 0;
    }

    public static boolean isRuined(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_RUINED);
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

        try {
            mode = TobaccoBarrelMode.valueOf(tag.getString("Mode"));
        } catch (Exception ignored) {
            mode = TobaccoBarrelMode.IDLE;
        }
    }
}