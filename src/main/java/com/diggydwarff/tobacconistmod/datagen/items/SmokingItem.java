package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class SmokingItem extends Item {

    public SmokingItem(Properties properties) {
        super(properties);
    }

    private String tooltip;

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean shouldEmitMouthSmoke(ItemStack stack) {
        return stack.isDamageableItem()
                && stack.getDamageValue() > 0
                && stack.getDamageValue() < stack.getMaxDamage();
    }

    public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokelevel, ItemStack tobaccoStack) {
        Vec3 look = player.getLookAngle();

        level.playSound(
                null,
                player.getX(), player.getY() + 1.4, player.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.05F,
                1.0F
        );

        Random rand = new Random();
        for (int i = 0; i < 5; ++i) {
            Vec3 newVec = new Vec3(rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D);
            newVec = newVec.multiply(0.01D, 0.01D, 0.01D);
            Vec3 mergeVec = look.add(newVec);
            level.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX() + mergeVec.x,
                    player.getY() + 1.4 + mergeVec.y,
                    player.getZ() + mergeVec.z,
                    smokelevel, 0, 0, 0, 0
            );
        }

        player.addEffect(new MobEffectInstance(
                ModEffects.NICOTINE.get(),
                500,
                0,
                false,
                false,
                true
        ));

        applyQualityHealthBonus(player, tobaccoStack);
        applyConfiguredAdditionalEffects(player);
    }

    protected void applyConfiguredAdditionalEffects(Player player) {
        for (String entry : TobacconistConfig.COMMON.additionalEffects.get()) {
            try {
                String[] parts = entry.split(",");
                if (parts.length < 3) continue;

                String effectId = parts[0].trim();
                int duration = Integer.parseInt(parts[1].trim());
                int amplifier = Integer.parseInt(parts[2].trim());

                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectId));
                if (effect == null) continue;

                player.addEffect(new MobEffectInstance(
                        effect,
                        duration,
                        amplifier,
                        false,
                        false,
                        true
                ));
            } catch (Exception ignored) {
                // ignore bad config entries
            }
        }
    }

    protected void applyQualityHealthBonus(Player player, ItemStack tobaccoStack) {
        if (tobaccoStack == null || tobaccoStack.isEmpty()) return;

        int quality = TobaccoCuringHelper.getQuality(tobaccoStack);
        int agedDays = TobaccoBarrelBlockEntity.getAgedDays(tobaccoStack);

        int duration = 0;
        int amplifier = 0;

        if (quality >= 98) {
            duration = 120; // 6 sec
            amplifier = 1;  // Regen II
        } else if (quality >= 90) {
            duration = 100; // 5 sec
            amplifier = 0;  // Regen I
        } else if (quality >= 70) {
            duration = 60;  // 3 sec
            amplifier = 0;  // Regen I
        }

        if (duration <= 0) return;

        int ageBonus = Math.min(200, (agedDays / 30) * 20); // +1 sec per 30 days, cap +10 sec
        duration += ageBonus;

        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                duration,
                amplifier,
                false,
                false,
                true
        ));
    }
}