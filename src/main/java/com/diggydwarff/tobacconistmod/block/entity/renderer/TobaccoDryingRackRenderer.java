package com.diggydwarff.tobacconistmod.block.entity.renderer;

import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Renders the rack post extensions used while the rack stands over a campfire. */
public class TobaccoDryingRackRenderer implements BlockEntityRenderer<TobaccoDryingRackBlockEntity> {

    private static final ResourceLocation RACK_TEXTURE =
            new ResourceLocation("tobacconistmod", "textures/block/tobacco_drying_rack_stage_0.png");

    public TobaccoDryingRackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TobaccoDryingRackBlockEntity rack, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!rack.getBlockState().getValue(TobaccoDryingRackBlock.OVER_CAMPFIRE)) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(RACK_TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.0F, -1.0F, 0.0F);

        float extensionTop = 15.95F / 16F;
        float lo = 0.562162F / 16F;
        float loMax = 2.032432F / 16F;
        float hi = 13.967568F / 16F;
        float hiMax = 15.437838F / 16F;
        renderBox(poseStack, consumer, lo, 0.0F, hi, loMax, extensionTop, hiMax, packedLight);
        renderBox(poseStack, consumer, hi, 0.0F, hi, hiMax, extensionTop, hiMax, packedLight);
        renderBox(poseStack, consumer, lo, 0.0F, lo, loMax, extensionTop, loMax, packedLight);
        renderBox(poseStack, consumer, hi, 0.0F, lo, hiMax, extensionTop, loMax, packedLight);

        poseStack.popPose();
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
                           float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ, int light) {
        PoseStack.Pose pose = poseStack.last();

        float u0 = 76.0F / 144.0F;
        float u1 = 78.0F / 144.0F;
        float v0 = 2.0F / 192.0F;
        float v1 = 28.0F / 192.0F;

        vertex(consumer, pose, minX, minY, minZ, u0, v1, 0, 0, -1, light);
        vertex(consumer, pose, maxX, minY, minZ, u1, v1, 0, 0, -1, light);
        vertex(consumer, pose, maxX, maxY, minZ, u1, v0, 0, 0, -1, light);
        vertex(consumer, pose, minX, maxY, minZ, u0, v0, 0, 0, -1, light);
        vertex(consumer, pose, minX, minY, maxZ, u0, v1, 0, 0, 1, light);
        vertex(consumer, pose, minX, maxY, maxZ, u0, v0, 0, 0, 1, light);
        vertex(consumer, pose, maxX, maxY, maxZ, u1, v0, 0, 0, 1, light);
        vertex(consumer, pose, maxX, minY, maxZ, u1, v1, 0, 0, 1, light);
        vertex(consumer, pose, minX, minY, minZ, u0, v1, -1, 0, 0, light);
        vertex(consumer, pose, minX, maxY, minZ, u0, v0, -1, 0, 0, light);
        vertex(consumer, pose, minX, maxY, maxZ, u1, v0, -1, 0, 0, light);
        vertex(consumer, pose, minX, minY, maxZ, u1, v1, -1, 0, 0, light);
        vertex(consumer, pose, maxX, minY, minZ, u0, v1, 1, 0, 0, light);
        vertex(consumer, pose, maxX, minY, maxZ, u1, v1, 1, 0, 0, light);
        vertex(consumer, pose, maxX, maxY, maxZ, u1, v0, 1, 0, 0, light);
        vertex(consumer, pose, maxX, maxY, minZ, u0, v0, 1, 0, 0, light);
        vertex(consumer, pose, minX, minY, minZ, u0, v1, 0, -1, 0, light);
        vertex(consumer, pose, minX, minY, maxZ, u0, v0, 0, -1, 0, light);
        vertex(consumer, pose, maxX, minY, maxZ, u1, v0, 0, -1, 0, light);
        vertex(consumer, pose, maxX, minY, minZ, u1, v1, 0, -1, 0, light);
        vertex(consumer, pose, minX, maxY, minZ, u0, v1, 0, 1, 0, light);
        vertex(consumer, pose, maxX, maxY, minZ, u1, v1, 0, 1, 0, light);
        vertex(consumer, pose, maxX, maxY, maxZ, u1, v0, 0, 1, 0, light);
        vertex(consumer, pose, minX, maxY, maxZ, u0, v0, 0, 1, 0, light);
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                        float x, float y, float z, float u, float v,
                        float nx, float ny, float nz, int light) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }
}
