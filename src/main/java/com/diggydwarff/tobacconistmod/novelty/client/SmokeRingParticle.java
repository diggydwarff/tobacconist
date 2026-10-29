package com.diggydwarff.tobacconistmod.novelty.client;

import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.novelty.NoveltyPattern;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

/** Real campfire smoke sections form a ring, then loosen into ordinary drifting smoke. */
final class SmokeRingParticle extends CampfireSmokeParticle {
    static final class Motion {
        Vec3 center, velocity;
        int age;
        final int points;
        long lastTick = Long.MIN_VALUE;
        Motion(Vec3 center, Vec3 velocity, int points) { this.center = center; this.velocity = velocity; this.points = points; }
        void tick(ClientLevel level) {
            if (lastTick == level.getGameTime()) return;
            lastTick = level.getGameTime(); age++;
            var airflow = CreateCompat.getSmokeAirflow(level, center);
            if (airflow.active()) velocity = velocity.add(airflow.x()*airflow.strength(),airflow.y()*airflow.strength(),airflow.z()*airflow.strength());
            if (velocity.lengthSqr() > .14*.14) velocity = velocity.normalize().scale(.14);
            center = center.add(velocity).add(0,.002,0); velocity = velocity.scale(.991);
        }
    }
    private final Motion motion;
    private final Vec3 radial;
    private final double radius;
    private final float strength;
    private final SpriteSet sprites;
    private final int loosenAt;
    private final float opacity;
    private final boolean idle;
    SmokeRingParticle(ClientLevel level, Vec3 position, NoveltyPattern.Sample sample, SpriteSet sprites, Motion motion) {
        super(level,position.x,position.y,position.z,sample.velocity().x,sample.velocity().y,sample.velocity().z,false);
        this.sprites = sprites; this.motion = motion;
        setColor(((sample.color()>>16)&255)/255F,((sample.color()>>8)&255)/255F,(sample.color()&255)/255F);
        radial = sample.offset().lengthSqr() < .000001 ? new Vec3(1,0,0) : sample.offset().normalize();
        radius = sample.offset().length(); strength = sample.size();idle=strength<=.45F;
        lifetime = idle ? 32+random.nextInt(10) : 78 + random.nextInt(20); loosenAt = 32 + random.nextInt(21);
        opacity = .58F + random.nextFloat() * .32F;
        alpha = opacity; quadSize = sectionSize(); pickSprite(sprites);
    }
    private float sectionSize() { return (idle ? .018F + age * .0005F : .048F + age * .0012F) * strength * (float)Math.sqrt(32.0 / motion.points); }
    @Override public void tick() {
        var airflow = CreateCompat.getSmokeAirflow(level,new Vec3(x,y,z));
        if (airflow.intakeCapture()) { remove(); return; }
        motion.tick(level);
        if (age >= loosenAt && airflow.active()) {
            xd += airflow.x()*airflow.strength(); yd += airflow.y()*airflow.strength(); zd += airflow.z()*airflow.strength();
        }
        super.tick();
        if (age < loosenAt) {
            Vec3 point = motion.center.add(radial.scale(radius + motion.age * .0045 * strength));
            setPos(point.x,point.y,point.z);
            xd = motion.velocity.x + radial.x*.0045*strength;
            yd = motion.velocity.y + radial.y*.0045*strength + .003;
            zd = motion.velocity.z + radial.z*.0045*strength;
        } else { yd += .0003; }
        quadSize = sectionSize();
        alpha = opacity * Math.min(1F,(lifetime-age)/30F);
        setSpriteFromAge(sprites);
    }
}
