package com.diggydwarff.tobacconistmod.particle;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TobacconistMod.MODID);

    public static final RegistryObject<SimpleParticleType> TOBACCO_SMOKE =
            PARTICLES.register("tobacco_smoke", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> TOBACCO_INDOOR_SMOKE =
            PARTICLES.register("tobacco_indoor_smoke", () -> new SimpleParticleType(true));

    private ModParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
