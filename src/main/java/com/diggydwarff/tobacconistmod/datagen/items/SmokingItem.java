package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.util.SmokeParticleHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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

    /**
     * Server-side action used when the player explicitly smokes this item while it is
     * equipped in the Curios mouth slot. Subclasses override this when they need to
     * consume durability/puffs.
     */
    public boolean smokeFromMouthSlot(Player player, ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        triggerSmokingEffectPlayer(player, level, 0, stack);
        return true;
    }

    public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokelevel, ItemStack tobaccoStack) {
        applySmokingEffects(player, level, tobaccoStack);
    }

    /** Shared smoking pipeline used by held items and Curios smoking. */
    public static void applySmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack) {
        applySmokingEffects(player, level, tobaccoStack, false);
    }

    /** Applies the larger smoke cloud used for a Hookah hose draw. */
    public static void applyHookahSmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack) {
        applySmokingEffects(player, level, tobaccoStack, true);
    }

    private static void applySmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, boolean hookahDraw) {
        Vec3 look = player.getLookAngle();

        level.playSound(
                null,
                player.getX(), player.getY() + 1.4, player.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.05F,
                1.0F
        );

        double smokeX;
        double smokeY;
        double smokeZ;
        double smokeDirX = look.x;
        double smokeDirZ = look.z;

        if (hookahDraw) {
            // Hookah exhales should visibly leave the front of the player's mouth rather than
            // spawning inside/behind the head. Normalize only the horizontal facing so looking
            // sharply up or down cannot collapse the forward offset back into the player model.
            Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
            if (horizontalLook.lengthSqr() < 1.0E-6D) {
                horizontalLook = Vec3.directionFromRotation(0.0F, player.getYRot());
            } else {
                horizontalLook = horizontalLook.normalize();
            }

            Vec3 mouth = player.getEyePosition().add(0.0D, -0.18D, 0.0D);
            smokeX = mouth.x + horizontalLook.x * 0.44D;
            smokeY = mouth.y + look.y * 0.03D;
            smokeZ = mouth.z + horizontalLook.z * 0.44D;
            smokeDirX = horizontalLook.x;
            smokeDirZ = horizontalLook.z;
        } else {
            smokeX = player.getX() + look.x * 0.12D;
            smokeY = player.getY() + 1.4D + look.y * 0.04D;
            smokeZ = player.getZ() + look.z * 0.12D;
        }

        if (hookahDraw) {
            SmokeParticleHelper.spawnServerHookahPuffSmoke(
                    level, smokeX, smokeY, smokeZ, smokeDirX, smokeDirZ
            );
        } else {
            SmokeParticleHelper.spawnServerMouthSmoke(
                    level, smokeX, smokeY, smokeZ, smokeDirX, smokeDirZ
            );
        }

        if (TobacconistConfig.areNicotineEffectsEnabled()) {
            player.addEffect(new MobEffectInstance(
                    ModEffects.NICOTINE,
                    500,
                    0,
                    false,
                    false,
                    true
            ));
        }

        applyQualityHealthBonus(player, tobaccoStack);
        applySecretBlendLegendaryBonus(player, tobaccoStack);
        applyConfiguredAdditionalEffects(player);
    }

    private static void applySecretBlendLegendaryBonus(Player player, ItemStack tobaccoStack) {
        if (player == null || tobaccoStack == null || tobaccoStack.isEmpty()) return;

        String blendName = TobaccoBlendHelper.getSecretBlendName(tobaccoStack);
        if (blendName.isEmpty()) return;

        TobacconistConfig.SecretBlendBonusDefinition bonus = TobacconistConfig.getSecretBlendBonus(blendName);
        if (bonus == null || bonus.effects().isEmpty()) return;

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound("tobacconistmod_secret_blend_bonus");
        String key = sanitizeSecretBlendKey(blendName);
        int next = root.getInt(key) + 1;

        if (next < bonus.threshold()) {
            root.putInt(key, next);
            persistent.put("tobacconistmod_secret_blend_bonus", root);
            return;
        }

        root.putInt(key, 0);
        persistent.put("tobacconistmod_secret_blend_bonus", root);

        for (TobacconistConfig.ConfiguredSmokingEffect configured : bonus.effects()) {
            try {
                var effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(configured.effectId()));
                if (effect.isEmpty()) continue;
                player.addEffect(new MobEffectInstance(
                        effect.get(),
                        configured.duration(),
                        configured.amplifier(),
                        false,
                        false,
                        true
                ));
            } catch (Exception ignored) {
                // ignore malformed legendary secret entries
            }
        }
    }

    private static String sanitizeSecretBlendKey(String blendName) {
        String normalized = blendName == null ? "" : blendName.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.replaceAll("[^a-z0-9]+", "_");
    }

    private static void applyConfiguredAdditionalEffects(Player player) {
        for (String entry : TobacconistConfig.COMMON.additionalEffects.get()) {
            try {
                String[] parts = entry.split(",");
                if (parts.length < 3) continue;

                String effectId = parts[0].trim();
                int duration = Integer.parseInt(parts[1].trim());
                int amplifier = Integer.parseInt(parts[2].trim());

                var effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectId));
                if (effect.isEmpty()) continue;

                player.addEffect(new MobEffectInstance(
                        effect.get(),
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

    private static void applyQualityHealthBonus(Player player, ItemStack tobaccoStack) {
        if (!TobacconistConfig.isQualitySystemEnabled()) return;
        if (tobaccoStack == null || tobaccoStack.isEmpty()) return;

        int quality = TobaccoProductQualityHelper.getEffectiveSmokingQuality(tobaccoStack);
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