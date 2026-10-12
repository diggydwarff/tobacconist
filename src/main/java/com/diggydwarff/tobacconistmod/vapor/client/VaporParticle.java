package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

/** A soft expanding puff, with bounded emission and the existing Create ventilation bridge. */
public final class VaporParticle extends TextureSheetParticle {
    private static SpriteSet sprites;
    private static ClientLevel emissionLevel;
    private static long emissionTick;
    private static int emitted;
    private final float initialSize;
    public VaporParticle(ClientLevel level, Vec3 position, Vec3 velocity, int vegetable, int draw) {
        super(level, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        xd = velocity.x; yd = velocity.y; zd = velocity.z;
        lifetime = 25 + vegetable / 3 + draw / 3;
        initialSize = .12F + vegetable * .0018F + draw * .002F;
        quadSize = initialSize; gravity = -.004F; friction = .97F; hasPhysics = true;
        setColor(.96F, .97F, .98F); alpha = .40F; pickSprite(sprites);
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
        super.tick();
        float progress = (float) age / lifetime;
        quadSize = initialSize * (1 + progress * 3.2F);
        alpha = .40F * Math.max(0, 1 - progress);
    }
    public static void puff(VaporPuffPayload payload) {
        var minecraft = Minecraft.getInstance(); ClientLevel level = minecraft.level;
        if (level == null || sprites == null) return;
        var player = level.getPlayerByUUID(payload.player());
        if (player == null || minecraft.player == null || player.distanceToSqr(minecraft.player) > 64 * 64) return;
        if (emissionLevel != level || emissionTick != level.getGameTime()) { emissionLevel = level; emissionTick = level.getGameTime(); emitted = 0; }
        int vg = Math.clamp(payload.vegetable(), 0, 100), draw = Math.clamp(payload.draw(), 6, 40);
        int count = 5 + vg / 5 + draw / 3;
        if (minecraft.options.particles().get() == net.minecraft.client.ParticleStatus.MINIMAL) count = Math.min(count, 6);
        else if (minecraft.options.particles().get() == net.minecraft.client.ParticleStatus.DECREASED) count /= 2;
        count = Math.min(count, 100 - emitted); emitted += count;
        Vec3 look = player.getLookAngle(), origin = player.getEyePosition().add(look.scale(.30)).add(0, -.12, 0);
        for (int i = 0; i < count; i++) {
            Vec3 spread = new Vec3((level.random.nextDouble() - .5) * .045, (level.random.nextDouble() - .5) * .035, (level.random.nextDouble() - .5) * .045);
            minecraft.particleEngine.add(new VaporParticle(level, origin.add(spread), look.scale(.04 + draw * .001).add(spread).add(0, .015, 0), vg, draw));
        }
    }
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet spriteSet) { sprites = spriteSet; }
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new VaporParticle(level, new Vec3(x, y, z), new Vec3(dx, dy, dz), 50, 20);
        }
    }
}
