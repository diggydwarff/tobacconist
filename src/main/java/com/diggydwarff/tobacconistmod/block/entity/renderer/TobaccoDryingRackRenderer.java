package com.diggydwarff.tobacconistmod.block.entity.renderer;

import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TobaccoDryingRackRenderer implements BlockEntityRenderer<TobaccoDryingRackBlockEntity> {

    public TobaccoDryingRackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TobaccoDryingRackBlockEntity rack, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        ItemStack stack = rack.getStoredLeaf();

        int count = Math.min(rack.getLeafCount(), TobaccoDryingRackBlockEntity.MAX_LEAVES);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        int cols = 4;
        float startX = 0.22F;
        float startZ = 0.22F;
        float spacing = 0.18F;

        boolean fireRack = rack.getBlockState()
                .getValue(TobaccoDryingRackBlock.OVER_CAMPFIRE);

        float y = fireRack ? 0.76F : 0.51F;

        if (rack.getBlockState().getValue(com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock.OVER_CAMPFIRE)) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(
                    new net.minecraft.resources.ResourceLocation("tobacconistmod", "textures/block/tobacco_drying_rack_block.png")
            ));

            poseStack.pushPose();

            // Render into the block below
            poseStack.translate(0.0F, -1.0F, 0.0F);

            float legSize = 1.0F / 16.0F;

// NW
            renderBox(poseStack, consumer,
                    2F / 16F, 2F / 16F, 2F / 16F,
                    3F / 16F, 16F / 16F, 3F / 16F,
                    packedLight);

// NE
            renderBox(poseStack, consumer,
                    13F / 16F, 2F / 16F, 2F / 16F,
                    14F / 16F, 16F / 16F, 3F / 16F,
                    packedLight);

// S
            renderBox(poseStack, consumer,
                    2F / 16F, 2F / 16F, 13F / 16F,
                    3F / 16F, 16F / 16F, 14F / 16F,
                    packedLight);

// SE
            renderBox(poseStack, consumer,
                    13F / 16F, 2F / 16F, 13F / 16F,
                    14F / 16F, 16F / 16F, 14F / 16F,
                    packedLight);

            poseStack.popPose();
        }

        for (int i = 0; i < count; i++) {
            int row = i / cols;
            int col = i % cols;

            poseStack.pushPose();
            poseStack.translate(startX + col * spacing, y, startZ + row * spacing);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.24F, 0.24F, 0.24F);

            ItemStack renderStack = stack.copyWithCount(1);

            itemRenderer.renderStatic(
                    renderStack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    rack.getLevel(),
                    0
            );

            poseStack.popPose();
        }
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
                           float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ,
                           int light) {

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // north
        vertex(consumer, pose, normal, minX, minY, minZ, 0, 0, 0, 0, -1, light);
        vertex(consumer, pose, normal, maxX, minY, minZ, 1, 0, 0, 0, -1, light);
        vertex(consumer, pose, normal, maxX, maxY, minZ, 1, 1, 0, 0, -1, light);
        vertex(consumer, pose, normal, minX, maxY, minZ, 0, 1, 0, 0, -1, light);

        // south
        vertex(consumer, pose, normal, minX, minY, maxZ, 0, 0, 0, 0, 1, light);
        vertex(consumer, pose, normal, minX, maxY, maxZ, 0, 1, 0, 0, 1, light);
        vertex(consumer, pose, normal, maxX, maxY, maxZ, 1, 1, 0, 0, 1, light);
        vertex(consumer, pose, normal, maxX, minY, maxZ, 1, 0, 0, 0, 1, light);

        // west
        vertex(consumer, pose, normal, minX, minY, minZ, 0, 0, -1, 0, 0, light);
        vertex(consumer, pose, normal, minX, maxY, minZ, 0, 1, -1, 0, 0, light);
        vertex(consumer, pose, normal, minX, maxY, maxZ, 1, 1, -1, 0, 0, light);
        vertex(consumer, pose, normal, minX, minY, maxZ, 1, 0, -1, 0, 0, light);

        // east
        vertex(consumer, pose, normal, maxX, minY, minZ, 0, 0, 1, 0, 0, light);
        vertex(consumer, pose, normal, maxX, minY, maxZ, 1, 0, 1, 0, 0, light);
        vertex(consumer, pose, normal, maxX, maxY, maxZ, 1, 1, 1, 0, 0, light);
        vertex(consumer, pose, normal, maxX, maxY, minZ, 0, 1, 1, 0, 0, light);

        // down
        vertex(consumer, pose, normal, minX, minY, minZ, 0, 0, 0, -1, 0, light);
        vertex(consumer, pose, normal, minX, minY, maxZ, 0, 1, 0, -1, 0, light);
        vertex(consumer, pose, normal, maxX, minY, maxZ, 1, 1, 0, -1, 0, light);
        vertex(consumer, pose, normal, maxX, minY, minZ, 1, 0, 0, -1, 0, light);

        // up
        vertex(consumer, pose, normal, minX, maxY, minZ, 0, 0, 0, 1, 0, light);
        vertex(consumer, pose, normal, maxX, maxY, minZ, 1, 0, 0, 1, 0, light);
        vertex(consumer, pose, normal, maxX, maxY, maxZ, 1, 1, 0, 1, 0, light);
        vertex(consumer, pose, normal, minX, maxY, maxZ, 0, 1, 0, 1, 0, light);
    }

    private void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                        float x, float y, float z, float u, float v,
                        float nx, float ny, float nz, int light) {
        consumer.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}