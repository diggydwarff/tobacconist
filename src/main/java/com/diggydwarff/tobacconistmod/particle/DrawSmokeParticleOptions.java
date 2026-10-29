package com.diggydwarff.tobacconistmod.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import com.diggydwarff.tobacconistmod.backport.ByteBufCodecs;
import com.diggydwarff.tobacconistmod.backport.StreamCodec;
import net.minecraft.util.Mth;

/** Carries server-measured draw strength so every client renders the same size of exhale. */
public record DrawSmokeParticleOptions(float strength) implements ParticleOptions {
    public static final MapCodec<DrawSmokeParticleOptions> CODEC = Codec.FLOAT.fieldOf("strength")
            .xmap(DrawSmokeParticleOptions::new, DrawSmokeParticleOptions::strength);
    public static final StreamCodec<FriendlyByteBuf, DrawSmokeParticleOptions> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(DrawSmokeParticleOptions::new, DrawSmokeParticleOptions::strength).cast();

    public static final ParticleOptions.Deserializer<DrawSmokeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override public DrawSmokeParticleOptions fromCommand(ParticleType<DrawSmokeParticleOptions> type, com.mojang.brigadier.StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
            reader.expect(' '); return new DrawSmokeParticleOptions(reader.readFloat());
        }
        @Override public DrawSmokeParticleOptions fromNetwork(ParticleType<DrawSmokeParticleOptions> type, net.minecraft.network.FriendlyByteBuf buffer) { return new DrawSmokeParticleOptions(buffer.readFloat()); }
    };
    @Override public void writeToNetwork(net.minecraft.network.FriendlyByteBuf buffer) { buffer.writeFloat(strength); }
    @Override public String writeToString() { return net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + " " + strength; }

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
