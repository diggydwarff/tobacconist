package com.diggydwarff.tobacconistmod.event;

import net.minecraftforge.fml.common.Mod;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "tobacconistmod")
public class ModPlayerEvents {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        // Genuine players are hand-rolling and retain full quality. Forge FakePlayers represent
        // generic crafting automation and receive the configured penalty.
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof FakePlayer)) {
            return;
        }
        TobaccoProductQualityHelper.applyGenericAutomationPenalty(event.getCrafting());
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getEntity().hasEffect(ModEffects.NICOTINE.get())) {
            return;
        }

        MobEffectInstance effect = event.getEntity().getEffect(ModEffects.NICOTINE.get());
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
