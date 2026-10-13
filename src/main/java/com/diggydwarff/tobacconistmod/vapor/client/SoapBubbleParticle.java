package com.diggydwarff.tobacconistmod.vapor.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/** Vanilla bubble artwork with an air-safe lifetime and gentle upward drift. */
public final class SoapBubbleParticle extends TextureSheetParticle {
    private static SpriteSet idleSprites;
    private final float initialSize;
    public static void idle(ClientLevel level,net.minecraft.world.phys.Vec3 origin,net.minecraft.world.phys.Vec3 look) {
        if(idleSprites==null) return;
        net.minecraft.client.Minecraft.getInstance().particleEngine.add(new SoapBubbleParticle(level,origin.x,origin.y,origin.z,look.x*.02,.004,look.z*.02,idleSprites,true));
    }
    SoapBubbleParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        this(level,x,y,z,dx,dy,dz,sprites,false);
    }
    private SoapBubbleParticle(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites,boolean idle) {
        super(level, x, y, z, dx, dy, dz);
        xd = dx * .45; yd = Math.max(.008, dy * .25 + .012); zd = dz * .45;
        lifetime = idle ? 36+random.nextInt(14) : 50 + random.nextInt(50); gravity = -.0015F; friction = .985F; hasPhysics = true;
        quadSize = initialSize = idle ? .018F+random.nextFloat()*.012F : .045F + random.nextFloat() * .045F;
        float tint = random.nextFloat(); setColor(.78F + .18F * tint, .92F - .13F * tint, 1F);
        alpha = .85F; pickSprite(sprites);
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override public void tick() {
        super.tick();
        quadSize = initialSize * (1 + (float) age / lifetime * .5F);
        alpha = .85F * Math.min(1F, (lifetime - age) / 15F);
        if (onGround) remove();
    }
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites;idleSprites=sprites; }
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new SoapBubbleParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
