package com.diggydwarff.tobacconistmod.novelty;

import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pure geometry shared by emission and verification, with a fixed per-puff bound. */
public final class NoveltyPattern {
    public enum Style { PAPER, RING, SOUL, SOUL_MOTE, STAR, CONSTELLATION, PETAL, NOTE, SUN_SMOKE, ECHO_PULSE, SOUL_EMBER, SUN_GLEAM, ECHO_MOTE }
    private static final int[] CONFETTI_COLORS = {0xFF244D,0xFF8514,0xFFE21B,0x36ED55,0x16DFFF,0x7350FF,0xF128D8};
    public record Sample(Style style, Vec3 offset, Vec3 velocity, int color, float size, int delay) {
        public Sample(Style style, Vec3 offset, Vec3 velocity, int color, float size) { this(style,offset,velocity,color,size,0); }
    }
    public static final int MAX_PARTICLES = 96;
    private NoveltyPattern() {}
    public static List<Sample> build(NoveltyContent.Kind kind,int requestedDraw,int color,Vec3 look,long seed) {
        int draw=Math.clamp(requestedDraw,1,30); double strength=draw/30.0;
        Vec3 forward=look.lengthSqr()<.000001?new Vec3(0,0,1):look.normalize();
        Vec3 reference=Math.abs(forward.y)>.95?new Vec3(0,0,1):new Vec3(0,1,0);
        Vec3 right=forward.cross(reference).normalize(),up=right.cross(forward).normalize();
        Random random=new Random(seed);List<Sample> result=new ArrayList<>();
        switch(kind) {
            case CONFETTI -> {
                int count=draw==30?96:4+draw,paletteOffset=random.nextInt(CONFETTI_COLORS.length);
                for(int i=0;i<count;i++) {
                    Vec3 spread=right.scale((random.nextDouble()-.5)*.16).add(up.scale((random.nextDouble()-.5)*.12));
                    Vec3 velocity=forward.scale(.035+random.nextDouble()*.055).add(spread).add(0,.015,0);
                    result.add(new Sample(Style.PAPER,spread,velocity,CONFETTI_COLORS[(i+paletteOffset)%7],
                            .8F+random.nextFloat()*.4F,draw==30?i/24:0));
                }
            }
            case WIZARD -> {
                int rings=draw==30?2:1;
                for(int ring=0;ring<rings;ring++) {
                    double phase=random.nextDouble()*Math.PI*2,phase2=random.nextDouble()*Math.PI*2,stretch=.90+random.nextDouble()*.20;
                    for(int i=0;i<32;i++) {
                        double angle=i*Math.PI*2/32;
                        Vec3 radial=right.scale(Math.cos(angle)*stretch).add(up.scale(Math.sin(angle)/stretch));
                        float base=(float)(.65+strength*.35);
                        double radius=.12*base*(1+.13*Math.sin(angle*3+phase)+.06*Math.cos(angle*5+phase2));
                        float thickness=base*(.80F+random.nextFloat()*.40F);
                        result.add(new Sample(Style.RING,radial.scale(radius),forward.scale(.065),0xFFFFFF,thickness,ring*8));
                    }
                }
            }
            case SOUL -> {
                // Uneven curling wisps and tiny glass-bright motes replace the rigid helix.
                int wisps=draw==30?3:1,points=6+draw/5;
                for(int wisp=0;wisp<wisps;wisp++) {
                    double phase=random.nextDouble()*Math.PI*2,lean=(random.nextDouble()-.5)*.12;
                    double curl=.045+random.nextDouble()*.045;
                    for(int i=0;i<points;i++) {
                        double t=i/(double)(points-1),angle=phase+t*Math.PI*1.7;
                        Vec3 offset=right.scale(Math.sin(angle)*curl+lean*t).add(up.scale(t*.22+Math.cos(angle)*.025)).add(forward.scale(t*.16));
                        Vec3 velocity=forward.scale(.024).add(right.scale(Math.cos(angle)*.005)).add(0,.018+t*.008,0);
                        result.add(new Sample(Style.SOUL,offset,velocity,i%3==0?0xDFE9FF:0xA6C0FF,
                                (float)(.8-t*.4),wisp*3+i/3));
                    }
                }
                for(int i=0;i<6+draw/2;i++) {
                    Vec3 offset=right.scale((random.nextDouble()-.5)*.3).add(up.scale(random.nextDouble()*.25)).add(forward.scale(random.nextDouble()*.2));
                    result.add(new Sample(Style.SOUL_MOTE,offset,forward.scale(.018).add(0,.012+random.nextDouble()*.014,0),
                            i%3==0?0xF4F4FF:i%3==1?0xB9D9FF:0xC9BDFF,.3F+random.nextFloat()*.3F,i/4));
                }
                // A few familiar native soul flames accompany the silver-blue wisps.
                for(int i=0;i<(draw==30 ? 5 : 2);i++) {
                    Vec3 offset=right.scale((random.nextDouble()-.5)*.18).add(up.scale(random.nextDouble()*.12));
                    result.add(new Sample(Style.SOUL_EMBER,offset,forward.scale(.023).add(0,.015,0),0xFFFFFF,
                            .40F+random.nextFloat()*.20F,i*4));
                }
            }
            case PETAL -> {
                int count=draw==30 ? 30 : 4+draw/2;
                for(int i=0;i<count;i++) {
                    Vec3 spread=right.scale((random.nextDouble()-.5)*.14).add(up.scale((random.nextDouble()-.5)*.08));
                    result.add(new Sample(Style.PETAL,spread,forward.scale(.035+random.nextDouble()*.025).add(spread.scale(.4)).add(0,.012,0),
                            0xFFFFFF,.7F+random.nextFloat()*.4F,draw==30 ? i/8 : 0));
                }
            }
            case WHISTLE -> {
                for(int i=0;i<2+draw/4;i++) {
                    Vec3 offset=right.scale((random.nextDouble()-.5)*.12).add(up.scale(random.nextDouble()*.08));
                    int hue=net.minecraft.util.Mth.hsvToRgb((i*.17F+draw/40F)%1F,.65F,1F);
                    result.add(new Sample(Style.NOTE,offset,forward.scale(.025).add(0,.018+random.nextDouble()*.008,0),hue,.75F+random.nextFloat()*.3F,draw==30 ? i*3 : i*2));
                }
            }
            case SUN -> {
                int rays=draw==30 ? 12 : 8,sections=draw==30 ? 4 : 3;
                double phase=random.nextDouble()*Math.PI*2;
                for(int ray=0;ray<rays;ray++) {
                    double angle=ray*Math.PI*2/rays+phase;
                    Vec3 radial=right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
                    for(int point=0;point<sections;point++) {
                        double radius=(.085+point*.065)*(.65+strength*.35);
                        result.add(new Sample(Style.SUN_SMOKE,radial.scale(radius),forward.scale(.047),
                                point%2==0?0xFFE6AB:0xFFF3D4,.55F+point*.08F,0));
                    }
                }
                for(int i=0;i<3+draw/6;i++) {
                    double angle=random.nextDouble()*Math.PI*2,radius=.07+random.nextDouble()*.12;
                    Vec3 offset=right.scale(Math.cos(angle)*radius).add(up.scale(Math.sin(angle)*radius));
                    result.add(new Sample(Style.SUN_GLEAM,offset,forward.scale(.024).add(offset.scale(.05)).add(0,.008,0),
                            i%2==0 ? 0xFFF9C7 : 0xFFD65C,.34F+random.nextFloat()*.16F,i/3));
                }
            }
            case ECHO -> {
                int pulses=draw==30?3:1;
                for(int i=0;i<pulses;i++) {
                    Vec3 drift=forward.scale(.026).add(right.scale((random.nextDouble()-.5)*.006));
                    result.add(new Sample(Style.ECHO_PULSE,Vec3.ZERO,drift,i%2==0 ? 0x88DCFF : 0xB6A9FF,
                            (float)(.65+strength*.45),i*8));
                }
                // Two loose streams of cyan/violet light drift through the fading waves.
                double phase=random.nextDouble()*Math.PI*2;
                int motes=draw==30 ? 24 : 6+draw/5;
                for(int i=0;i<motes;i++) {
                    double t=i/(double)motes,angle=phase+t*Math.PI*3,radius=.05+t*.13;
                    Vec3 offset=right.scale(Math.cos(angle)*radius).add(up.scale(Math.sin(angle)*radius)).add(forward.scale(t*.12));
                    result.add(new Sample(Style.ECHO_MOTE,offset,forward.scale(.019).add(0,.006,0),
                            i%3==0 ? 0xA6EFFF : i%3==1 ? 0x8EB5FF : 0xC1ACFF,.28F+random.nextFloat()*.18F,i/2));
                }
            }
            case ASTRAL -> {
                if(draw==30) AstralConstellations.append(result,forward,right,up,random);
                int sparkles=draw==30?24:5+draw/3;double width=draw==30?1.6:.16+strength*.5;
                for(int i=0;i<sparkles;i++) {
                    Vec3 offset=right.scale((random.nextDouble()-.5)*width).add(up.scale((random.nextDouble()-.5)*width*.8)).add(forward.scale(random.nextDouble()*.15));
                    Vec3 drift=forward.scale(.018+random.nextDouble()*.014).add(0,.003+random.nextDouble()*.007,0);
                    result.add(new Sample(Style.STAR,offset,drift,i%3==0?0x9ABFFF:i%3==1?0xD2ACFF:0xF5EFFF,
                            .45F+random.nextFloat()*.55F,draw==30?i/6:0));
                }
            }
        }
        result.sort(java.util.Comparator.comparingInt(Sample::delay));
        if(result.size()>MAX_PARTICLES) throw new IllegalStateException("Novelty puff exceeds particle budget");
        return List.copyOf(result);
    }
    public static List<Sample> idle(NoveltyContent.Kind kind,Vec3 look,long seed) {
        var full=build(kind,4,0,look,seed);var small=new ArrayList<Sample>();
        if(kind==NoveltyContent.Kind.WIZARD) {
            for(int i=0;i<12;i++) {
                var source=full.get(i*32/12);
                small.add(new Sample(Style.RING,source.offset().scale(.065/(.12*(.65+4/30.0*.35))),
                        source.velocity().scale(.34),0xFFFFFF,.4F));
            }
        } else if(kind==NoveltyContent.Kind.SUN) {
            for(int i=0;i<8;i++) {
                var source=full.get(i*3);
                small.add(new Sample(Style.SUN_SMOKE,source.offset().normalize().scale(.055),source.velocity().scale(.35),source.color(),.35F));
            }
            var gleam=full.stream().filter(p->p.style()==Style.SUN_GLEAM).findFirst().orElseThrow();
            small.add(new Sample(gleam.style(),gleam.offset().scale(.3),gleam.velocity().scale(.3),gleam.color(),.22F));
        } else if(kind==NoveltyContent.Kind.ECHO) {
            var pulse=full.getFirst();
            small.add(new Sample(Style.ECHO_PULSE,Vec3.ZERO,pulse.velocity().scale(.4),pulse.color(),.35F));
            for(var mote:full.stream().filter(p->p.style()==Style.ECHO_MOTE).limit(2).toList())
                small.add(new Sample(mote.style(),mote.offset().scale(.3),mote.velocity().scale(.4),mote.color(),.20F));
        } else {
            int count=Math.min(full.size(),kind==NoveltyContent.Kind.CONFETTI ? 3 : kind==NoveltyContent.Kind.ECHO ? 1 : 4);
            for(int i=0;i<count;i++) {
                var source=full.get(i);
                if(kind==NoveltyContent.Kind.SOUL && i==3) source=full.stream().filter(p->p.style()==Style.SOUL_MOTE).findFirst().orElseThrow();
                small.add(new Sample(source.style(),source.offset().scale(.18),source.velocity().scale(.4).add(0,.004,0),source.color(),source.size()*.55F));
            }
        }
        if(kind==NoveltyContent.Kind.SOUL && Math.floorMod(seed,3)==0) {
            var ember=full.stream().filter(p->p.style()==Style.SOUL_EMBER).findFirst().orElseThrow();
            small.add(new Sample(ember.style(),ember.offset().scale(.2),ember.velocity().scale(.35),ember.color(),.25F));
        }
        return List.copyOf(small);
    }
}
