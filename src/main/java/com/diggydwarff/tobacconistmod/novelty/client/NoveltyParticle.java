package com.diggydwarff.tobacconistmod.novelty.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.novelty.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.List;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class NoveltyParticle extends TextureSheetParticle {
    private static final EnumMap<NoveltyPattern.Style, SpriteSet> SPRITES = new EnumMap<>(NoveltyPattern.Style.class);
    private static final ArrayDeque<Burst> BURSTS = new ArrayDeque<>();
    private static ClientLevel emissionLevel;
    private static final class Burst {
        final Vec3 origin; final java.util.UUID player; final List<NoveltyPattern.Sample> samples; int next, age;
        final java.util.Map<Integer,SmokeRingParticle.Motion> rings = new java.util.HashMap<>();
        Burst(Vec3 origin, java.util.UUID player, List<NoveltyPattern.Sample> samples) { this.origin = origin; this.player = player; this.samples = samples; }
    }
    private final NoveltyPattern.Style style;
    private final float baseSize, phase;
    private final SpriteSet sprites;
    private NoveltyParticle(ClientLevel level, Vec3 position, NoveltyPattern.Sample sample) {
        super(level, position.x, position.y, position.z, sample.velocity().x, sample.velocity().y, sample.velocity().z);
        style = sample.style(); sprites = SPRITES.get(style);
        xd = sample.velocity().x; yd = sample.velocity().y; zd = sample.velocity().z;
        phase = random.nextFloat() * 6.28F; friction = style == NoveltyPattern.Style.PAPER ? .97F : .99F;
        gravity = style == NoveltyPattern.Style.PAPER ? .045F : style == NoveltyPattern.Style.PETAL ? .02F : -.002F;
        hasPhysics = style == NoveltyPattern.Style.PAPER || style == NoveltyPattern.Style.PETAL;
        lifetime = style == NoveltyPattern.Style.CONSTELLATION ? 82 : style == NoveltyPattern.Style.STAR ? 50 + random.nextInt(20)
                : style == NoveltyPattern.Style.SOUL ? 48 + random.nextInt(18) : style == NoveltyPattern.Style.SOUL_MOTE ? 38 : 55;
        quadSize = baseSize = (style == NoveltyPattern.Style.PAPER ? .035F : style == NoveltyPattern.Style.RING ? .055F : .045F) * sample.size();
        setColor(((sample.color() >> 16) & 255) / 255F, ((sample.color() >> 8) & 255) / 255F, (sample.color() & 255) / 255F);
        alpha = .9F; pickSprite(sprites);
        if (style == NoveltyPattern.Style.PAPER) {
            float shade = .94F + random.nextFloat() * .06F;
            rCol *= shade; gCol *= shade; bCol *= shade;
        }
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override public int getLightColor(float partial) { return style != NoveltyPattern.Style.PAPER ? 0xF000F0 : super.getLightColor(partial); }
    @Override public void tick() {
        var airflow = CreateCompat.getSmokeAirflow(level, new Vec3(x,y,z));
        if (airflow.intakeCapture()) { remove(); return; }
        if (airflow.active()) {
            xd += airflow.x() * airflow.strength(); yd += airflow.y() * airflow.strength(); zd += airflow.z() * airflow.strength();
            double speed = Math.sqrt(xd*xd + yd*yd + zd*zd);
            if (speed > .14) { xd *= .14/speed; yd *= .14/speed; zd *= .14/speed; }
        }
        if (style == NoveltyPattern.Style.SOUL) { xd += Math.sin(age*.12 + phase)*.00035; zd += Math.cos(age*.12 + phase)*.00035; }
        super.tick();
        float fade = Math.min(1F, (lifetime - age) / 18F);
        alpha = fade * .9F;
        if (style == NoveltyPattern.Style.SOUL) {
            alpha = Math.min(1F, age / 4F) * Math.min(1F, (lifetime-age) / 20F) * .7F;
            quadSize = baseSize * (1F + age * .012F);
        }
        if (style == NoveltyPattern.Style.SOUL_MOTE) {
            alpha = fade * (.35F + .5F * (float)Math.pow(Math.sin(age*.25 + phase),2));
            quadSize = baseSize * (.8F + .25F * (float)Math.sin(age*.2 + phase));
            oRoll = roll; roll += .025F;
        }
        if (style == NoveltyPattern.Style.PAPER) { oRoll = roll; roll += .17F * Math.sin(phase); }
        if (style == NoveltyPattern.Style.PETAL) {
            oRoll=roll;roll += .03F*(float)Math.sin(age*.11+phase);
            xd += Math.sin(age*.13+phase)*.0004;zd += Math.cos(age*.1+phase)*.0004;
            setSpriteFromAge(sprites);
        }
        if (style == NoveltyPattern.Style.SOUL || style == NoveltyPattern.Style.SOUL_MOTE
                || style == NoveltyPattern.Style.SOUL_EMBER || style == NoveltyPattern.Style.SUN_GLEAM) setSpriteFromAge(sprites);
        if (style == NoveltyPattern.Style.SUN_GLEAM || style == NoveltyPattern.Style.ECHO_MOTE) {
            alpha = fade * (.40F + .45F*(float)Math.pow(Math.sin(age*.16+phase),2));
            quadSize = baseSize * (.85F + .2F*(float)Math.sin(age*.12+phase));
            oRoll=roll;roll+=.018F;
        }
        if (style == NoveltyPattern.Style.STAR) {
            alpha *= .6F + .4F * (float)Math.pow(Math.sin(age*.2 + phase),2);
            quadSize = baseSize * (.85F + .2F * (float)Math.sin(age*.18 + phase));
            oRoll = roll; roll += .013F;
            setSprite(sprites.get((age / 4 + (int)(phase*3)) % 4, 3));
        }
        if (style == NoveltyPattern.Style.CONSTELLATION) {
            // Shared drift and stable star/link sizes preserve the drawing's shape.
            alpha *= .85F + .15F * (float)Math.pow(Math.sin(age*.1 + phase),2);
            setSprite(sprites.get(0,3));
        }
    }
    @Override public void render(VertexConsumer out, Camera camera, float partial) {
        if (style != NoveltyPattern.Style.PAPER) { super.render(out, camera, partial); return; }
        Quaternionf rotation = new Quaternionf(camera.rotation()).rotateZ(Mth.lerp(partial, oRoll, roll));
        Vec3 center = new Vec3(Mth.lerp(partial,xo,x),Mth.lerp(partial,yo,y),Mth.lerp(partial,zo,z)).subtract(camera.getPosition());
        float fold = .35F + .65F * Math.abs((float)Math.cos((age+partial)*.17F+phase));
        // Sample the opaque center of the native sprite to produce crisp rectangular paper.
        float u0 = Mth.lerp(.45F,getU0(),getU1()), u1 = Mth.lerp(.55F,getU0(),getU1());
        float v0 = Mth.lerp(.45F,getV0(),getV1()), v1 = Mth.lerp(.55F,getV0(),getV1());
        // Match vanilla particle winding so back-face culling keeps the paper visible.
        paperVertex(out,rotation,center,-1,-fold,u1,v1,partial);
        paperVertex(out,rotation,center,-1, fold,u1,v0,partial);
        paperVertex(out,rotation,center, 1, fold,u0,v0,partial);
        paperVertex(out,rotation,center, 1,-fold,u0,v1,partial);
    }
    private void paperVertex(VertexConsumer out, Quaternionf rotation, Vec3 center, float vx,float vy,float u,float v,float partial) {
        Vector3f point = new Vector3f(vx*quadSize,vy*quadSize*.55F,0).rotate(rotation).add((float)center.x,(float)center.y,(float)center.z);
        out.vertex(point.x,point.y,point.z).uv(u,v).color(rCol,gCol,bCol,alpha).uv2(getLightColor(partial)).endVertex();
    }
    public static void providers(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NoveltyContent.CONFETTI_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.PAPER, sprites));
        event.registerSpriteSet(NoveltyContent.RING_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.RING, sprites));
        event.registerSpriteSet(NoveltyContent.SOUL_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.SOUL, sprites));
        event.registerSpriteSet(NoveltyContent.SOUL_MOTE_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.SOUL_MOTE, sprites));
        event.registerSpriteSet(NoveltyContent.STAR_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.STAR, sprites));
        event.registerSpriteSet(NoveltyContent.PETAL_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.PETAL, sprites));
        event.registerSpriteSet(NoveltyContent.NOTE_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.NOTE, sprites));
        event.registerSpriteSet(NoveltyContent.SUN_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.SUN_SMOKE, sprites));
        event.registerSpriteSet(NoveltyContent.ECHO_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.ECHO_PULSE, sprites));
        event.registerSpriteSet(NoveltyContent.SOUL_EMBER_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.SOUL_EMBER, sprites));
        event.registerSpriteSet(NoveltyContent.SUN_GLEAM_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.SUN_GLEAM, sprites));
        event.registerSpriteSet(NoveltyContent.ECHO_MOTE_PARTICLE.get(), sprites -> provider(NoveltyPattern.Style.ECHO_MOTE, sprites));
    }
    private static ParticleProvider<SimpleParticleType> provider(NoveltyPattern.Style style, SpriteSet sprites) {
        SPRITES.put(style, sprites);
        if (style == NoveltyPattern.Style.STAR) SPRITES.put(NoveltyPattern.Style.CONSTELLATION,sprites);
        return (type, level, x,y,z,dx,dy,dz) -> create(level, new Vec3(x,y,z), new NoveltyPattern.Sample(style, Vec3.ZERO, new Vec3(dx,dy,dz), 0xFFFFFF, 1));
    }
    private static boolean smokeStyle(NoveltyPattern.Style style) { return style==NoveltyPattern.Style.RING || style==NoveltyPattern.Style.SUN_SMOKE; }
    private static Particle create(ClientLevel level, Vec3 position, NoveltyPattern.Sample sample) {
        if(sample.style()==NoveltyPattern.Style.ECHO_PULSE) return new EchoPulseParticle(level,position,sample,SPRITES.get(sample.style()));
        return smokeStyle(sample.style())
                ? new SmokeRingParticle(level, position, sample, SPRITES.get(sample.style()), new SmokeRingParticle.Motion(position.subtract(sample.offset()), sample.velocity(), 32))
                : new NoveltyParticle(level, position, sample);
    }
    private static void spawn(ClientLevel level, Burst burst, Vec3 origin, NoveltyPattern.Sample sample) {
        if (!SPRITES.containsKey(sample.style())) return;
        if (!smokeStyle(sample.style())) {
            Minecraft.getInstance().particleEngine.add(create(level,origin.add(sample.offset()),sample));
            return;
        }
        var motion = burst.rings.computeIfAbsent(sample.delay(), delay -> new SmokeRingParticle.Motion(origin,sample.velocity(),
                (int)burst.samples.stream().filter(point -> smokeStyle(point.style()) && point.delay()==delay).count()));
        Minecraft.getInstance().particleEngine.add(new SmokeRingParticle(level,origin.add(sample.offset()),sample,SPRITES.get(sample.style()),motion));
    }
    public static int idle(NoveltyContent.Kind kind, net.minecraft.world.entity.player.Player player, Vec3 origin, Vec3 look, int budget) {
        var level = Minecraft.getInstance().level;
        if (level == null || budget <= 0) return 0;
        var samples = NoveltyPattern.idle(kind,look,player.getUUID().getLeastSignificantBits() ^ level.getGameTime());
        if (smokeStyle(samples.get(0).style()) && samples.size() > budget) return 0;
        var burst = new Burst(origin,player.getUUID(),samples); int emitted = 0;
        for (var sample : samples) {
            if (emitted >= budget) break;
            if (SPRITES.containsKey(sample.style())) { spawn(level,burst,origin,sample); emitted++; }
        }
        return emitted;
    }
    public static void puff(NoveltyPuffPayload payload) {
        var minecraft = Minecraft.getInstance(); var level = minecraft.level;
        if (level == null || minecraft.player == null || payload.kind() < 0 || payload.kind() >= NoveltyContent.Kind.values().length) return;
        if (level != emissionLevel) { BURSTS.clear(); emissionLevel = level; }
        var player = level.getPlayerByUUID(payload.player());
        if (player == null || player.distanceToSqr(minecraft.player) > 64*64 || BURSTS.size() >= 32) return;
        var samples = NoveltyPattern.build(NoveltyContent.Kind.values()[payload.kind()], payload.draw(), payload.color(), player.getLookAngle(), player.getUUID().getLeastSignificantBits() ^ level.getGameTime());
        ParticleStatus status = minecraft.options.particles().get();
        if (status != ParticleStatus.ALL) {
            int stride = smokeStyle(samples.get(0).style()) ? 2 : status == ParticleStatus.MINIMAL ? 5 : 2;
            var reduced = new java.util.ArrayList<NoveltyPattern.Sample>();
            for (int i=0;i<samples.size();i+=stride) reduced.add(samples.get(i));
            samples = List.copyOf(reduced);
        }
        BURSTS.add(new Burst(player.getEyePosition().add(player.getLookAngle().scale(.38)).add(0,-.12,0), player.getUUID(), samples));
    }
    @SubscribeEvent public static void emit(ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance(); var level = minecraft.level;
        if (level == null || level != emissionLevel) { BURSTS.clear(); emissionLevel = level; return; }
        if (minecraft.isPaused()) return;
        int budget = 96;
        var iterator = BURSTS.iterator();
        while (iterator.hasNext()) {
            var burst = iterator.next();
            var source = level.getPlayerByUUID(burst.player);
            if (source == null || !source.isAlive() || source.isSpectator()
                    || minecraft.player == null || source.distanceToSqr(minecraft.player) > 64*64) {
                iterator.remove(); continue;
            }
            while (budget > 0 && burst.next < burst.samples.size() && burst.samples.get(burst.next).delay() <= burst.age) {
                var sample = burst.samples.get(burst.next++);
                Vec3 origin = burst.origin;
                if (sample.delay() > 0) {
                    var player = level.getPlayerByUUID(burst.player);
                    if (player != null) origin = player.getEyePosition().add(player.getLookAngle().scale(.38)).add(0,-.12,0);
                }
                spawn(level,burst,origin,sample);
                budget--;
            }
            burst.age++;
            if (burst.next == burst.samples.size()) iterator.remove();
        }
    }
}
