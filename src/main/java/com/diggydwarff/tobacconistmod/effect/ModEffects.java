package com.diggydwarff.tobacconistmod.effect;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, TobacconistMod.MODID);

    public static final DeferredHolder<MobEffect, NicotineEffect> NICOTINE = MOB_EFFECTS.register(
            "nicotine",
            () -> new NicotineEffect(MobEffectCategory.NEUTRAL, 2495014)
    );

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
