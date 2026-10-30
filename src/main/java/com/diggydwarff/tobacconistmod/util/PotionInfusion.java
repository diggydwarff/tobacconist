package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

/** One measured treatment per tobacco item. Finished products retain the same finite budget. */
public final class PotionInfusion {
    public static final String KEY = "PotionInfusion";
    public static final int FLUID_DOSE = 50;
    public static final int CRAFT_BATCH = 5;
    public static final int DRAWS = 3;
    public static final int PULSES_PER_DRAW = 3;

    private PotionInfusion() {}

    public static boolean enabled() { return TobacconistConfig.SERVER.enablePotionInfusion.get(); }

    /** Null means root data; otherwise the packed snapshot is authoritative, never the wrapper. */
    private static CompoundTag owner(CompoundTag root) {
        if (root.contains(KEY, Tag.TAG_COMPOUND)) return root;
        return root.getCompound("PackedTobaccoData");
    }

    public static CompoundTag data(ItemStack stack) {
        CompoundTag root = LegacyItemTags.getTag(stack);
        return root == null ? new CompoundTag() : owner(root).getCompound(KEY).copy();
    }

    public static boolean isTreated(ItemStack stack) { return !data(stack).isEmpty(); }

    public static boolean canTreat(ItemStack stack) {
        return enabled() && !stack.isEmpty() && !isTreated(stack)
                && (TobaccoCuringHelper.isLooseTobacco(stack)
                || (TobaccoProcessingHelper.isShisha(stack) && stack.getDamageValue() == 0))
                && !com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity.isRuined(stack);
    }

    /** Reject unsupported mixtures as a whole instead of silently stripping their drawbacks. */
    public static CompoundTag prepare(PotionContents potion) {
        if (!enabled() || potion == null || !potion.hasEffects()) return new CompoundTag();
        ListTag effects = new ListTag();
        for (MobEffectInstance effect : potion.getAllEffects()) {
            String id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();
            if (!allowed(id) || effect.getEffect().value().isInstantenous()
                    || effect.isInfiniteDuration() || effect.getDuration() <= 0 || effects.size() >= 4)
                return new CompoundTag();
            int budget = Math.min(TobacconistConfig.SERVER.infusionMaxTotalTicks.get(),
                    (int) Math.floor(effect.getDuration() * TobacconistConfig.SERVER.infusionDurationScale.get()));
            int pulse = budget / (DRAWS * PULSES_PER_DRAW);
            if (pulse < 1) return new CompoundTag();
            CompoundTag stored = new CompoundTag();
            stored.putString("Effect", id);
            stored.putInt("Amplifier", Math.min(effect.getAmplifier(), TobacconistConfig.SERVER.infusionMaxAmplifier.get()));
            stored.putInt("PulseTicks", pulse);
            effects.add(stored);
        }
        if (effects.isEmpty()) return new CompoundTag();
        CompoundTag result = new CompoundTag();
        result.putInt("DrawsLeft", DRAWS);
        result.put("Effects", effects);
        result.putInt("Color", potion.getColor());
        return result;
    }

    public static boolean allowed(String effect) {
        return TobacconistConfig.SERVER.infusionAllowedEffects.get().contains(effect);
    }

    public static ItemStack treat(ItemStack source, PotionContents potion) {
        if (!canTreat(source)) return ItemStack.EMPTY;
        CompoundTag infusion = prepare(potion);
        if (infusion.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = source.copyWithCount(1);
        CompoundTag root = LegacyItemTags.getTag(result);
        if (root == null) root = new CompoundTag();
        root.put(KEY, infusion);
        LegacyItemTags.setTag(result, root);
        return result;
    }

    /** Spend only on a full draw that can start a new course; busy draws never reset its timer. */
    public static void smoke(Player player, ItemStack tobacco, float strength) {
        if (!enabled() || strength < .999F || !player.isAlive() || player.isSpectator()) return;
        CompoundTag infusion = data(tobacco);
        int left = Math.clamp(infusion.getInt("DrawsLeft"), 0, DRAWS);
        if (left <= 0 || !PotionInfusionEvents.queue(player, infusion)) return;
        infusion.putInt("DrawsLeft", left - 1);
        CompoundTag root = LegacyItemTags.getTag(tobacco);
        if (root == null) return;
        if (root.contains(KEY, Tag.TAG_COMPOUND)) root.put(KEY, infusion);
        else {
            CompoundTag packed = root.getCompound("PackedTobaccoData").copy();
            packed.put(KEY, infusion);
            root.put("PackedTobaccoData", packed);
        }
        LegacyItemTags.setTag(tobacco, root);
    }

    /** Pipe smoking reconstructs a temporary tobacco stack; commit its spent budget to the pipe. */
    public static void commitPacked(ItemStack pipe, ItemStack smokedTobacco) {
        if (!isTreated(smokedTobacco)) return;
        CompoundTag root = LegacyItemTags.getTag(pipe);
        CompoundTag packed = LegacyItemTags.getTag(smokedTobacco);
        if (root == null || packed == null) return;
        root.put("PackedTobaccoData", packed.copy());
        LegacyItemTags.setTag(pipe, root);
    }
}
