package com.diggydwarff.tobacconistmod.particle;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.particles.ParticleType;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import com.diggydwarff.tobacconistmod.backport.StreamCodec;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.Registry;
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

    public static final RegistryObject<ParticleType<DrawSmokeParticleOptions>> TOBACCO_DRAW_SMOKE =
            PARTICLES.register("tobacco_draw_smoke", () -> new ParticleType<DrawSmokeParticleOptions>(true, DrawSmokeParticleOptions.DESERIALIZER) {
                @Override
                public com.mojang.serialization.Codec<DrawSmokeParticleOptions> codec() {
                    return DrawSmokeParticleOptions.CODEC.codec();
                }

            });

    public static final RegistryObject<SimpleParticleType> VAPE_VAPOR =
            PARTICLES.register("vape_vapor", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SOAP_BUBBLE =
            PARTICLES.register("soap_bubble", () -> new SimpleParticleType(false));

    private ModParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
