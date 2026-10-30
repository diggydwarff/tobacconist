package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.util.SmokeParticleHelper;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import com.diggydwarff.tobacconistmod.util.TobaccoBlendHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoSpecialProcessingHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoSmokeData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class SmokingItem extends Item {

    public SmokingItem(Properties properties) {
        super(properties);
    }

    public boolean canSmoke(ItemStack stack) {
        return !stack.isEmpty() && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage());
    }

    protected InteractionResultHolder<ItemStack> beginDraw(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canSmoke(stack)) return InteractionResultHolder.pass(stack);
        if (!SmokingDraws.canStart(player, stack)) return InteractionResultHolder.consume(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return SmokingDraws.duration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // A custom native hand transform avoids vanilla drinking/eating bobbing and sounds.
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide() && entity instanceof Player player
                && (!player.isAlive() || player.containerMenu != player.inventoryMenu
                || !TobacconistConfig.areSmokingDrawAnimationsEnabled())) {
            player.stopUsingItem();
            return;
        }
        if (TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            SmokingDraws.drawSound(level, entity, stack, getUseDuration(stack, entity) - remainingUseDuration);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        completeDraw(stack, level, entity, getUseDuration(stack, entity));
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        completeDraw(stack, level, entity, getUseDuration(stack, entity) - timeLeft);
    }

    private void completeDraw(ItemStack stack, Level level, LivingEntity entity, int elapsed) {
        if (elapsed < SmokingDraws.MIN_DRAW_TICKS) return;
        if (entity instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                && player.containerMenu == player.inventoryMenu && player.isUsingItem()
                && player.getUseItem() == stack && player.getItemInHand(player.getUsedItemHand()) == stack
                && canSmoke(stack)) {
            ItemStack used = stack.copy();
            float strength = SmokingDraws.strength(elapsed, used);
            if (smokeFromMouthSlot(player, player.serverLevel(), stack, strength)) SmokingDraws.recover(player, used);
        }
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
        return smokeFromMouthSlot(player, level, stack, 1.0F);
    }

    public boolean smokeFromMouthSlot(Player player, ServerLevel level, ItemStack stack, float strength) {
        if (stack.isEmpty()) return false;
        triggerSmokingEffectPlayer(player, level, 0, stack, strength);
        return true;
    }

    public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokelevel, ItemStack tobaccoStack) {
        applySmokingEffects(player, level, tobaccoStack);
    }

    public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokelevel, ItemStack tobaccoStack, float strength) {
        applySmokingEffects(player, level, tobaccoStack, false, strength);
    }

    /** Shared smoking pipeline used by held items and Curios smoking. */
    public static void applySmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack) {
        applySmokingEffects(player, level, tobaccoStack, false, 1.0F);
    }

    /** Applies the larger smoke cloud used for a Hookah hose draw. */
    public static void applyHookahSmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack) {
        applyHookahSmokingEffects(player, level, tobaccoStack, 1.0F);
    }

    public static void applyHookahSmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, float strength) {
        applySmokingEffects(player, level, tobaccoStack, true, strength);
    }

    public static void applyRelicSmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, float strength) {
        applyRelicSmokingEffects(player, level, tobaccoStack, strength, strength);
    }

    public static void applyRelicSmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, float smokeStrength, float drawStrength) {
        applySmokingEffects(player, level, tobaccoStack, false, smokeStrength, true, drawStrength);
    }

    private static void applySmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, boolean hookahDraw, float strength) {
        applySmokingEffects(player, level, tobaccoStack, hookahDraw, strength, true);
    }

    private static void applySmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, boolean hookahDraw, float strength, boolean smoke) {
        applySmokingEffects(player, level, tobaccoStack, hookahDraw, strength, smoke, strength);
    }

    private static void applySmokingEffects(Player player, ServerLevel level, ItemStack tobaccoStack, boolean hookahDraw, float strength, boolean smoke, float drawStrength) {
        if (smoke && TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
            SmokingDraws.queueExhale(player, tobaccoStack, hookahDraw, strength);
        } else if (smoke) {
            emitSmokingSmoke(player, level, tobaccoStack, hookahDraw, strength);
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
        applyConfiguredRuleEffects(player, tobaccoStack);
        com.diggydwarff.tobacconistmod.util.PotionInfusion.smoke(player, tobaccoStack, drawStrength);
    }

    public static void emitSmokingSmoke(Player player, ServerLevel level, ItemStack tobaccoStack, boolean hookahDraw) {
        emitSmokingSmoke(player, level, tobaccoStack, hookahDraw, 1.0F);
    }

    public static void emitSmokingSmoke(Player player, ServerLevel level, ItemStack tobaccoStack, boolean hookahDraw, float strength) {
        Vec3 look = player.getLookAngle();

        level.playSound(
                null,
                player.getX(), player.getY() + 1.4, player.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.025F + 0.025F * strength,
                1.0F
        );

        double smokeX;
        double smokeY;
        double smokeZ;
        double smokeDirX = look.x;
        double smokeDirZ = look.z;

        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        horizontalLook = horizontalLook.lengthSqr() < 1.0E-6D
                ? Vec3.directionFromRotation(0.0F, player.getYRot()) : horizontalLook.normalize();
        Vec3 mouth = player.getEyePosition().add(0.0D, -0.18D, 0.0D);
        double forwardOffset = hookahDraw ? 0.44D : 0.34D;
        smokeX = mouth.x + horizontalLook.x * forwardOffset;
        smokeY = mouth.y + look.y * 0.03D;
        smokeZ = mouth.z + horizontalLook.z * forwardOffset;
        smokeDirX = horizontalLook.x;
        smokeDirZ = horizontalLook.z;

        if (TobacconistConfig.areSmokingDrawAnimationsEnabled() || strength < 1F) {
            SmokeParticleHelper.spawnServerDrawSmoke(level, smokeX, smokeY, smokeZ, smokeDirX, smokeDirZ, hookahDraw, strength);
        } else if (hookahDraw) {
            SmokeParticleHelper.spawnServerHookahPuffSmoke(level, smokeX, smokeY, smokeZ, smokeDirX, smokeDirZ);
        } else {
            SmokeParticleHelper.spawnServerMouthSmoke(level, smokeX, smokeY, smokeZ, smokeDirX, smokeDirZ);
        }

        if (TobaccoSpecialProcessingHelper.isHaunted(tobaccoStack)) {
            SmokeParticleHelper.spawnServerHauntedWisps(
                    level, smokeX, smokeY, smokeZ, smokeDirX, smokeDirZ, hookahDraw, strength
            );
        }

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

    private static void applyConfiguredRuleEffects(Player player, ItemStack tobaccoStack) {
        if (player == null || tobaccoStack == null || tobaccoStack.isEmpty()) return;

        List<TobacconistConfig.SmokeEffectRule> rules = TobacconistConfig.getEffectRules();
        if (rules.isEmpty()) return;

        TobaccoSmokeData data = TobaccoSmokeData.from(tobaccoStack);
        if (data == null) return;

        for (TobacconistConfig.SmokeEffectRule rule : rules) {
            if (!rule.matches(data)) continue;

            for (TobacconistConfig.ConfiguredSmokingEffect configured : rule.effects()) {
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
                    // ignore malformed effect rule entries
                }
            }
        }
    }
}
