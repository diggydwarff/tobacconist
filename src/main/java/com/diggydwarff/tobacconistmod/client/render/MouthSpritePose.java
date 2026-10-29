package com.diggydwarff.tobacconistmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/** Pixel coordinates refer to the supplied 16x16 sprite; the mouthpiece stays on the lips. */
public record MouthSpritePose(float tipX, float tipY, float yaw, float roll, float scale) {
    public static final MouthSpritePose CIGALIKE = new MouthSpritePose(14.5F, 1.5F, 55F, 135F, .20F);
    // Equivalent to the wooden pipe layer rotations plus item/generated FIXED yaw (180°).
    // These sprites have neutral FIXED transforms, so apply the complete orientation here.
    public static final MouthSpritePose BUBBLE_PIPE = new MouthSpritePose(14.5F, 1.5F, 25F, 180F, .30F);
    // The supplied Petal sprite is normalized to 16x16 without interpolating colors.
    public static final MouthSpritePose PETAL_PIPE = new MouthSpritePose(15.5F, .5F, 25F, 180F, .30F);
    public static final MouthSpritePose SPECIAL_PIPE = new MouthSpritePose(14.5F, 1.5F, 25F, 180F, .30F);
    public void apply(PoseStack pose) {
        pose.translate(.025F, -.100F, this == CIGALIKE ? -.280F : -.265F);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(roll));
        pose.scale(scale, scale, scale);
        pose.translate(.5F - tipX / 16F, tipY / 16F - .5F, 0F);
    }
}
