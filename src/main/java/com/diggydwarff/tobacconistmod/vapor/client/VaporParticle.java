package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.vapor.VaporCloudProfile;
import com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.ArrayDeque;
import java.util.UUID;

/** A streamed exhale with denser, larger high-VG billows and a global emission budget. */
@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class VaporParticle extends TextureSheetParticle {
    private static SpriteSet sprites;
    private static ClientLevel emissionLevel;
    private static final ArrayDeque<Exhale> exhales = new ArrayDeque<>();
    private final float initialSize, initialOpacity;
    private static final class Exhale {
        final UUID player; final int vg, draw, count; final VaporCloudProfile profile;
        int age;
        int delay = TobacconistConfig.areSmokingDrawAnimationsEnabled() ? SmokingDraws.EXHALE_DELAY_TICKS : 0;
        Exhale(VaporPuffPayload payload, ParticleStatus status) {
            player = payload.player(); vg = Math.clamp(payload.vegetable(), 0, 100); draw = Math.clamp(payload.draw(), 6, 40);
            profile = VaporCloudProfile.of(vg, draw, payload.output());
            count = status == ParticleStatus.MINIMAL ? Math.min(profile.particles(), 6) : status == ParticleStatus.DECREASED ? profile.particles() / 2 : profile.particles();
        }
    }
    public VaporParticle(ClientLevel level, Vec3 position, Vec3 velocity, int vegetable, int draw) {
        this(level, position, velocity, VaporCloudProfile.of(vegetable, draw));
    }
    private VaporParticle(ClientLevel level, Vec3 position, Vec3 velocity, VaporCloudProfile profile) {
        super(level, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        xd = velocity.x; yd = velocity.y; zd = velocity.z;
        lifetime = profile.lifetime(); initialSize = profile.size(); initialOpacity = profile.opacity();
        quadSize = initialSize; gravity = -.003F; friction = .975F; hasPhysics = true;
        setColor(.96F, .97F, .98F); alpha = initialOpacity; pickSprite(sprites);
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override public void tick() {
        var airflow = CreateCompat.getSmokeAirflow(level, new Vec3(x, y, z));
        if (airflow.intakeCapture()) { remove(); return; }
        if (airflow.active()) {
            xd += airflow.x() * airflow.strength(); yd += airflow.y() * airflow.strength(); zd += airflow.z() * airflow.strength();
            double speed = Math.sqrt(xd * xd + yd * yd + zd * zd);
            if (speed > .16) { xd *= .16 / speed; yd *= .16 / speed; zd *= .16 / speed; }
        }
        super.tick(); float progress = (float) age / lifetime;
        quadSize = initialSize * (1 + progress * 3.8F);
        alpha = initialOpacity * Math.min(1, (1 - progress) * 2);
    }
    public static void puff(VaporPuffPayload payload) {
        var minecraft = Minecraft.getInstance(); ClientLevel level = minecraft.level;
        if (level == null || sprites == null || minecraft.player == null) return;
        if (emissionLevel != level) { exhales.clear(); emissionLevel = level; }
        var player = level.getPlayerByUUID(payload.player());
        if (player == null || player.distanceToSqr(minecraft.player) > 64 * 64 || exhales.size() >= 32) return;
        exhales.add(new Exhale(payload, minecraft.options.particles().get()));
    }
    @SubscribeEvent public static void emit(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance(); ClientLevel level = minecraft.level;
        if (level != emissionLevel || level == null || minecraft.player == null) { exhales.clear(); emissionLevel = level; return; }
        if (minecraft.isPaused()) return;
        int budget = 96;
        for (var iterator = exhales.iterator(); iterator.hasNext();) {
            Exhale exhale = iterator.next(); var player = level.getPlayerByUUID(exhale.player);
            if (player == null || player.distanceToSqr(minecraft.player) > 64 * 64) { iterator.remove(); continue; }
            if (exhale.delay > 0) { exhale.delay--; continue; }
            int duration = exhale.profile.duration();
            int count = Math.min(budget, exhale.count * (exhale.age + 1) / duration - exhale.count * exhale.age / duration);
            budget -= count;
            Vec3 look = player.getLookAngle(), origin = player.getEyePosition().add(look.scale(.38)).add(0, -.12, 0);
            for (int i = 0; i < count; i++) {
                double width = .035 + exhale.vg * .0007;
                Vec3 spread = new Vec3((level.random.nextDouble() - .5) * width, (level.random.nextDouble() - .5) * width, (level.random.nextDouble() - .5) * width);
                minecraft.particleEngine.add(new VaporParticle(level, origin.add(spread), look.scale(.07 + exhale.draw * .0014).add(spread).add(0, .008, 0), exhale.profile));
            }
            if (++exhale.age >= duration) iterator.remove();
        }
    }
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet spriteSet) { sprites = spriteSet; }
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new VaporParticle(level, new Vec3(x, y, z), new Vec3(dx, dy, dz), 50, 20);
        }
    }
}
