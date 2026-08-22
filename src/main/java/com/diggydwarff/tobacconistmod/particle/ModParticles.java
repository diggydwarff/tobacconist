package com.diggydwarff.tobacconistmod.particle;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TobacconistMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOBACCO_SMOKE =
            PARTICLES.register("tobacco_smoke", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOBACCO_INDOOR_SMOKE =
            PARTICLES.register("tobacco_indoor_smoke", () -> new SimpleParticleType(true));

    private ModParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
