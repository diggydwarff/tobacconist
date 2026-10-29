package com.diggydwarff.tobacconistmod.novelty.client;

import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.novelty.NoveltyPattern;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Soft luminous ripples with traveling blue/violet highlights and orbiting pinpoints. */
final class EchoPulseParticle extends TextureSheetParticle {
    private final Vec3 right,up;
    private final float strength,phase;

    EchoPulseParticle(ClientLevel level,Vec3 position,NoveltyPattern.Sample sample,SpriteSet sprites) {
        super(level,position.x,position.y,position.z,sample.velocity().x,sample.velocity().y,sample.velocity().z);
        xd=sample.velocity().x;yd=sample.velocity().y;zd=sample.velocity().z;
        Vec3 forward=sample.velocity().lengthSqr()<.000001?new Vec3(0,0,1):sample.velocity().normalize();
        right=forward.cross(Math.abs(forward.y)>.95?new Vec3(0,0,1):new Vec3(0,1,0)).normalize();
        up=right.cross(forward).normalize();strength=sample.size();phase=random.nextFloat()*Mth.TWO_PI;
        lifetime=strength<.45F ? 28 : 44;friction=.98F;hasPhysics=false;pickSprite(sprites);
        setColor(((sample.color()>>16)&255)/255F,((sample.color()>>8)&255)/255F,(sample.color()&255)/255F);
    }

    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override public int getLightColor(float partial) { return 0xF000F0; }

    @Override public void tick() {
        var airflow=CreateCompat.getSmokeAirflow(level,new Vec3(x,y,z));
        if(airflow.intakeCapture()) { remove();return; }
        if(airflow.active()) { xd+=airflow.x()*airflow.strength();yd+=airflow.y()*airflow.strength();zd+=airflow.z()*airflow.strength(); }
        double speed=Math.sqrt(xd*xd+yd*yd+zd*zd);
        if(speed>.14) { xd*=.14/speed;yd*=.14/speed;zd*=.14/speed; }
        super.tick();
    }

    @Override public void render(VertexConsumer out,Camera camera,float partial) {
        float time=age+partial;
        float fade=Math.min(1,time/5F)*Math.min(1,(lifetime-time)/16F);
        Vec3 center=new Vec3(Mth.lerp(partial,xo,x),Mth.lerp(partial,yo,y),Mth.lerp(partial,zo,z)).subtract(camera.getPosition());
        double radius=(.045+time*.009)*strength;
        float u=Mth.lerp(.5F,getU0(),getU1()),v=Mth.lerp(.5F,getV0(),getV1());
        for(int i=0;i<64;i++) {
            double a=i*Math.PI*2/64,b=(i+1)*Math.PI*2/64;
            double ra=radius*(1+.045*Math.sin(a*3+phase+time*.04));
            double rb=radius*(1+.045*Math.sin(b*3+phase+time*.04));
            float highlight=.42F+.58F*(float)Math.pow(.5+.5*Math.cos(a*4-phase-time*.09),3);
            // A broad dim halo and a thin bright core keep the wave light and translucent.
            strip(out,center,a,b,ra,rb,.014*strength,u,v,fade*highlight*.18F);
            strip(out,center,a,b,ra,rb,.0035*strength,u,v,fade*highlight*.68F);
        }
        for(int i=0;i<4;i++) {
            double angle=phase+i*Math.PI/2+time*.035;
            Vec3 point=center.add(right.scale(Math.cos(angle)*radius)).add(up.scale(Math.sin(angle)*radius));
            mote(out,point,.016*strength,fade*.8F);
        }
    }

    private void strip(VertexConsumer out,Vec3 center,double a,double b,double ra,double rb,double width,float u,float v,float opacity) {
        vertex(out,center,a,ra-width,u,v,opacity);vertex(out,center,a,ra+width,u,v,opacity);
        vertex(out,center,b,rb+width,u,v,opacity);vertex(out,center,b,rb-width,u,v,opacity);
        vertex(out,center,b,rb-width,u,v,opacity);vertex(out,center,b,rb+width,u,v,opacity);
        vertex(out,center,a,ra+width,u,v,opacity);vertex(out,center,a,ra-width,u,v,opacity);
    }

    private void vertex(VertexConsumer out,Vec3 center,double angle,double radius,float u,float v,float opacity) {
        point(out,center.add(right.scale(Math.cos(angle)*radius)).add(up.scale(Math.sin(angle)*radius)),u,v,opacity);
    }

    private void mote(VertexConsumer out,Vec3 center,double size,float opacity) {
        Vec3[] points={center.add(up.scale(-size)),center.add(right.scale(size)),center.add(up.scale(size)),center.add(right.scale(-size))};
        float[] us={getU0(),getU1(),getU1(),getU0()},vs={getV1(),getV1(),getV0(),getV0()};
        for(int i=0;i<4;i++) point(out,points[i],us[i],vs[i],opacity);
        for(int i=3;i>=0;i--) point(out,points[i],us[i],vs[i],opacity);
    }

    private void point(VertexConsumer out,Vec3 point,float u,float v,float opacity) {
        out.addVertex((float)point.x,(float)point.y,(float)point.z).setUv(u,v)
                .setColor(rCol,gCol,bCol,opacity).setLight(0xF000F0);
    }
}
