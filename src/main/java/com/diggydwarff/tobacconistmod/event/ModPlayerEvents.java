package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "tobacconistmod")
public class ModPlayerEvents {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        // Genuine players are hand-rolling and keep full quality. Fake-player crafting is
        // automation, so it follows the same rule as the Vanilla/Create crafter hooks.
        if (event.getEntity().level().isClientSide() || !event.getEntity().isFakePlayer()) {
            return;
        }
        TobaccoProductQualityHelper.applyGenericAutomationPenalty(event.getCrafting());
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getEntity().hasEffect(ModEffects.NICOTINE)) {
            return;
        }

        MobEffectInstance effect = event.getEntity().getEffect(ModEffects.NICOTINE);
        int amplifier = effect != null ? effect.getAmplifier() : 0;

        float multiplier;
        switch (amplifier) {
            case 0 -> multiplier = 1.08f; // Nicotine I
            case 1 -> multiplier = 1.12f; // Nicotine II
            default -> multiplier = 1.15f;
        }

        event.setNewSpeed(event.getNewSpeed() * multiplier);
    }
}