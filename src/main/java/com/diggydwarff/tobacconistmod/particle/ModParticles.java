package com.diggydwarff.tobacconistmod.particle;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.particles.ParticleType;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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

    public static final DeferredHolder<ParticleType<?>, ParticleType<DrawSmokeParticleOptions>> TOBACCO_DRAW_SMOKE =
            PARTICLES.register("tobacco_draw_smoke", () -> new ParticleType<DrawSmokeParticleOptions>(true) {
                @Override
                public MapCodec<DrawSmokeParticleOptions> codec() {
                    return DrawSmokeParticleOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, DrawSmokeParticleOptions> streamCodec() {
                    return DrawSmokeParticleOptions.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VAPE_VAPOR =
            PARTICLES.register("vape_vapor", () -> new SimpleParticleType(false));

    private ModParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
