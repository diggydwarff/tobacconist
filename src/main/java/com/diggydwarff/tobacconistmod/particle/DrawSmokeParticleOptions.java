package com.diggydwarff.tobacconistmod.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/** Carries server-measured draw strength so every client renders the same size of exhale. */
public record DrawSmokeParticleOptions(float strength) implements ParticleOptions {
    public static final MapCodec<DrawSmokeParticleOptions> CODEC = Codec.FLOAT.fieldOf("strength")
            .xmap(DrawSmokeParticleOptions::new, DrawSmokeParticleOptions::strength);
    public static final StreamCodec<RegistryFriendlyByteBuf, DrawSmokeParticleOptions> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(DrawSmokeParticleOptions::new, DrawSmokeParticleOptions::strength).cast();

    public DrawSmokeParticleOptions {
        strength = Float.isFinite(strength) ? Mth.clamp(strength, 0.0F, 1.0F) : 0.0F;
    }

    public float scale() {
        return 0.25F + 0.85F * strength;
    }

    @Override
    public ParticleType<DrawSmokeParticleOptions> getType() {
        return ModParticles.TOBACCO_DRAW_SMOKE.get();
    }
}
