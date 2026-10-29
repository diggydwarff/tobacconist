package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.EnumMap;
import java.util.Map;

/** Collision and selection boxes exported from the supplied bench model. */
final class VaporBenchShape {
    private static final double[][] BOXES = {
            {0, 0, 0, 16, 11, 16},
            {2.5, 10, 2.5, 5, 14, 5},
            {3.25, 13, 3.25, 4.25, 15, 4.25},
            {11, 10, 2.5, 13.5, 14, 5},
            {11.75, 13, 3.25, 12.75, 15, 4.25},
            {6.5, 9.75, 1.42, 9.5, 12.75, 4.58},
            {6.42, 11.5, 1.5, 9.58, 14.5, 4.5},
            {7, 13, 2, 9, 16, 4},
            {7.5, 14.75, 3.25, 8.5, 15.75, 10.25},
            {7, 13.25, 8.75, 9, 15.25, 10.75},
            {6.25, 10, 8.25, 9.75, 13.2, 11.75},
            {6.92, 13, 9, 9.08, 14, 11}
    };
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    private VaporBenchShape() {}

    static VoxelShape forFacing(Direction direction) {
        return SHAPES.getOrDefault(direction, SHAPES.get(Direction.NORTH));
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> result = new EnumMap<>(Direction.class);
        // The supplied model's front points south before its blockstate rotation.
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int turns = switch (direction) { case SOUTH -> 0; case WEST -> 1; case NORTH -> 2; default -> 3; };
            VoxelShape shape = Shapes.empty();
            for (double[] source : BOXES) {
                double[] box = source.clone();
                for (int turn = 0; turn < turns; turn++) {
                    double x1 = 16 - box[5], z1 = box[0], x2 = 16 - box[2], z2 = box[3];
                    box[0] = x1; box[2] = z1; box[3] = x2; box[5] = z2;
                }
                shape = Shapes.or(shape, Block.box(box[0], box[1], box[2], box[3], box[4], box[5]));
            }
            result.put(direction, shape.optimize());
        }
        return result;
    }
}
