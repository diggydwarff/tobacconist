package com.diggydwarff.tobacconistmod.novelty;

import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Random;

/** Recognizable sky silhouettes; coordinates describe artwork, not a star catalogue. */
public final class AstralConstellations {
    private record Shape(double[][] nodes, int[] edges) {}
    private static Shape shape(double[][] nodes, int... edges) { return new Shape(nodes,edges); }
    private static final List<Shape> SHAPES = List.of(
            // Orion, Cassiopeia, the two Dippers and Cygnus.
            shape(new double[][]{{-.35,.45},{.35,.4},{-.15,.02},{0,0},{.15,-.02},{-.3,-.45},{.35,-.4}},0,1,0,2,2,3,3,4,4,1,2,5,4,6,5,6),
            shape(new double[][]{{-.55,.3},{-.3,-.15},{0,.25},{.3,-.2},{.55,.3}},0,1,1,2,2,3,3,4),
            shape(new double[][]{{-.6,.3},{-.35,.25},{-.1,.05},{.15,.12},{.5,.1},{.45,-.25},{.08,-.2}},0,1,1,2,2,3,3,4,4,5,5,6,6,3),
            shape(new double[][]{{-.5,-.3},{-.35,-.08},{-.15,.15},{.1,.2},{.35,.35},{.5,.1},{.25,-.04}},0,1,1,2,2,3,3,4,4,5,5,6,6,3),
            shape(new double[][]{{0,.5},{0,.1},{-.5,.05},{.5,.05},{0,-.45}},0,1,1,2,1,3,1,4),
            // Lyra, Scorpius, Leo, Draco and Aquila.
            shape(new double[][]{{-.35,.45},{-.15,.1},{.25,.15},{.4,-.25},{0,-.35}},0,1,1,2,2,3,3,4,4,1),
            shape(new double[][]{{-.5,.4},{-.25,.2},{-.3,-.05},{0,-.35},{.3,-.4},{.5,-.2},{.4,0}},0,1,1,2,2,3,3,4,4,5,5,6),
            shape(new double[][]{{-.5,.05},{-.3,.4},{0,.45},{.15,.2},{-.1,.05},{.35,-.2},{.55,-.35}},0,1,1,2,2,3,3,4,4,0,4,5,5,6,6,0),
            shape(new double[][]{{-.5,-.3},{-.3,-.05},{-.45,.3},{-.1,.45},{.15,.15},{.3,.25},{.5,.05},{.3,-.1}},0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,4),
            shape(new double[][]{{-.45,.3},{-.15,.08},{0,0},{.15,-.08},{.45,-.3},{.25,.35},{-.25,-.35}},0,1,1,2,2,3,3,4,2,5,2,6),
            // Pegasus, Crux, Corona, Delphinus and Sagitta.
            shape(new double[][]{{-.3,.3},{.3,.3},{.3,-.3},{-.3,-.3},{-.55,.45},{.55,.45},{-.55,-.4}},0,1,1,2,2,3,3,0,0,4,1,5,3,6),
            shape(new double[][]{{0,.5},{0,.05},{0,-.45},{-.3,.1},{.3,.1}},0,1,1,2,1,3,1,4),
            shape(new double[][]{{-.5,.05},{-.4,.3},{-.15,.45},{.15,.45},{.4,.3},{.5,.05}},0,1,1,2,2,3,3,4,4,5),
            shape(new double[][]{{-.2,.1},{0,.35},{.25,.15},{.02,-.08},{-.45,-.35}},0,1,1,2,2,3,3,0,0,4),
            shape(new double[][]{{-.5,-.05},{.25,.08},{.5,.1},{.15,.3},{.2,-.15}},0,1,1,2,2,3,2,4),
            // Triangulum, Cepheus, Perseus, Taurus and Andromeda.
            shape(new double[][]{{-.45,-.25},{.1,.4},{.5,-.15}},0,1,1,2,2,0),
            shape(new double[][]{{-.35,-.3},{.35,-.3},{.4,.15},{0,.5},{-.4,.15}},0,1,1,2,2,3,3,4,4,0),
            shape(new double[][]{{-.45,.4},{-.12,.22},{.05,0},{.35,.2},{.5,.4},{.3,-.4},{-.3,-.35}},0,1,1,2,2,3,3,4,2,5,2,6),
            shape(new double[][]{{-.45,.4},{-.15,.05},{0,-.2},{.2,.05},{.5,.4},{-.4,-.35}},0,1,1,2,2,3,3,4,2,5),
            shape(new double[][]{{-.55,-.3},{-.2,-.05},{.1,.05},{.4,.25},{.1,.45},{-.2,.35}},0,1,1,2,2,3,2,4,1,5)
    );
    private AstralConstellations() {}
    public static int count() { return SHAPES.size(); }
    static void append(List<NoveltyPattern.Sample> result,Vec3 forward,Vec3 right,Vec3 up,Random random) {
        Shape shape=SHAPES.get(random.nextInt(SHAPES.size()));
        double rotation=(random.nextDouble()-.5)*.4;
        Vec3 horizontal=right.scale(Math.cos(rotation)).add(up.scale(Math.sin(rotation)));
        Vec3 vertical=up.scale(Math.cos(rotation)).subtract(right.scale(Math.sin(rotation)));
        Vec3 drift=forward.scale(.022).add(0,.004,0);Vec3[] nodes=new Vec3[shape.nodes.length];
        for(int i=0;i<nodes.length;i++) {
            nodes[i]=horizontal.scale(shape.nodes[i][0]*1.25).add(vertical.scale(shape.nodes[i][1]*1.25));
            result.add(new NoveltyPattern.Sample(NoveltyPattern.Style.CONSTELLATION,nodes[i],drift,0xF5EFFF,1.35F));
        }
        for(int i=0;i<shape.edges.length;i+=2) {
            Vec3 from=nodes[shape.edges[i]],to=nodes[shape.edges[i+1]];
            int dots=Math.clamp((int)Math.ceil(from.distanceTo(to)/.09),2,7);
            for(int j=1;j<dots;j++) result.add(new NoveltyPattern.Sample(NoveltyPattern.Style.CONSTELLATION,
                    from.lerp(to,j/(double)dots),drift,0xAC8EEB,.32F));
        }
    }
}
