package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "tobacconistmod")
public class ModPlayerEvents {

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