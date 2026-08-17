package com.diggydwarff.tobacconistmod.block.entity.renderer;

import com.diggydwarff.tobacconistmod.block.custom.TobaccoDryingRackBlock;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class TobaccoDryingRackRenderer implements BlockEntityRenderer<TobaccoDryingRackBlockEntity> {

    private static final ResourceLocation RACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/block/tobacco_drying_rack_block.png");

    private ResourceLocation getLeafTexture(ItemStack stack) {
        if (stack.is(ModItems.SHADE_TOBACCO_LEAF.get())) {
            return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_shade.png");
        }
        if (stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get())) {
            return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_virginia.png");
        }
        if (stack.is(ModItems.BURLEY_TOBACCO_LEAF.get())) {
            return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_burley.png");
        }
        if (stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get())) {
            return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_oriental.png");
        }
        if (stack.is(ModItems.DOKHA_TOBACCO_LEAF.get())) {
            return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_dokha.png");
        }
        if (stack.is(ModItems.WILD_TOBACCO_LEAF.get())) {
            return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_wild.png");
        }

        return ResourceLocation.fromNamespaceAndPath("tobacconistmod", "textures/item/tobacco_leaf_burley.png");
    }
    public TobaccoDryingRackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TobaccoDryingRackBlockEntity rack, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        ItemStack stack = rack.getStoredLeaf();
        if (stack.isEmpty()) {
            return;
        }

        boolean fireRack = rack.getBlockState().getValue(TobaccoDryingRackBlock.OVER_CAMPFIRE);
        int count = Math.min(rack.getLeafCount(), TobaccoDryingRackBlockEntity.MAX_LEAVES);

        // rack posts / frame when above campfire
        if (fireRack) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(RACK_TEXTURE));

            poseStack.pushPose();
            poseStack.translate(0.0F, -1.0F, 0.0F);

            // Continue the chunky 2x2 posts down into the campfire block. Stop just short of
            // the rack block boundary so the extension never z-fights the model's bottom faces.
            float extensionTop = 15.95F / 16F;
            renderBox(poseStack, consumer, 2F / 16F, 2F / 16F, 2F / 16F, 4F / 16F, extensionTop, 4F / 16F, packedLight);
            renderBox(poseStack, consumer, 12F / 16F, 2F / 16F, 2F / 16F, 14F / 16F, extensionTop, 4F / 16F, packedLight);
            renderBox(poseStack, consumer, 2F / 16F, 2F / 16F, 12F / 16F, 4F / 16F, extensionTop, 14F / 16F, packedLight);
            renderBox(poseStack, consumer, 12F / 16F, 2F / 16F, 12F / 16F, 14F / 16F, extensionTop, 14F / 16F, packedLight);

            poseStack.popPose();
        }

        VertexConsumer leafConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getLeafTexture(stack)));

        float progress = Mth.clamp(rack.getDryProgressPercent() / 100.0F, 0.0F, 1.0F);

        float[] baseColor = getBaseLeafColor(stack);
        float[] curedColor = getCuredLeafColor(stack);

        float r = Mth.lerp(progress, baseColor[0], curedColor[0]);
        float g = Mth.lerp(progress, baseColor[1], curedColor[1]);
        float b = Mth.lerp(progress, baseColor[2], curedColor[2]);

        int cols = 4;
        // Keep the 4x4 leaf display fully inside the rack opening.
        float startX = 0.31F;
        float startZ = 0.31F;
        float spacing = 0.125F;
        // Keep leaves clearly above the wooden slats; the old ~0.01 block separation on the
        // normal rack could depth-fight at distance and make the top flicker in and out.
        float y = fireRack ? 0.69F : 0.55F;

        for (int i = 0; i < count; i++) {
            int row = i / cols;
            int col = i % cols;

            poseStack.pushPose();
            poseStack.translate(startX + col * spacing, y, startZ + row * spacing);

            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            renderLeafQuad(poseStack, leafConsumer, packedLight, r, g, b);

            poseStack.popPose();
        }
    }

    private void renderLeafQuad(PoseStack poseStack, VertexConsumer consumer, int light, float r, float g, float b) {
        // Slightly smaller leaves keep the grid clear of the surrounding frame.
        float minX = -0.055F;
        float maxX =  0.055F;
        float minY = -0.075F;
        float maxY =  0.075F;
        float z = 0.0F;

        PoseStack.Pose pose = poseStack.last();

        // entityCutoutNoCull makes this single quad visible from both sides.
        // Drawing a second coplanar back face caused the old movement shimmer/z-fighting.
        vertex(consumer, pose, minX, minY, z, 0F, 1F, 0F, 0F, 1F, light, r, g, b, 1F);
        vertex(consumer, pose, maxX, minY, z, 1F, 1F, 0F, 0F, 1F, light, r, g, b, 1F);
        vertex(consumer, pose, maxX, maxY, z, 1F, 0F, 0F, 0F, 1F, light, r, g, b, 1F);
        vertex(consumer, pose, minX, maxY, z, 0F, 0F, 0F, 0F, 1F, light, r, g, b, 1F);
    }

    private float[] getBaseLeafColor(ItemStack stack) {
        if (stack.is(ModItems.SHADE_TOBACCO_LEAF.get())) {
            return rgb(126, 170, 92);
        }
        if (stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get())) {
            return rgb(135, 181, 84);
        }
        if (stack.is(ModItems.BURLEY_TOBACCO_LEAF.get())) {
            return rgb(121, 162, 86);
        }
        if (stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get())) {
            return rgb(144, 176, 82);
        }
        if (stack.is(ModItems.DOKHA_TOBACCO_LEAF.get())) {
            return rgb(116, 154, 74);
        }
        if (stack.is(ModItems.WILD_TOBACCO_LEAF.get())) {
            return rgb(103, 147, 79);
        }
        return rgb(130, 170, 90);
    }

    private float[] getCuredLeafColor(ItemStack stack) {
        if (stack.is(ModItems.SHADE_TOBACCO_LEAF.get())) {
            return rgb(104, 86, 46);
        }
        if (stack.is(ModItems.VIRGINIA_TOBACCO_LEAF.get())) {
            return rgb(148, 112, 52);
        }
        if (stack.is(ModItems.BURLEY_TOBACCO_LEAF.get())) {
            return rgb(132, 102, 58);
        }
        if (stack.is(ModItems.ORIENTAL_TOBACCO_LEAF.get())) {
            return rgb(156, 118, 54);
        }
        if (stack.is(ModItems.DOKHA_TOBACCO_LEAF.get())) {
            return rgb(92, 62, 34);
        }
        if (stack.is(ModItems.WILD_TOBACCO_LEAF.get())) {
            return rgb(118, 92, 52);
        }
        return rgb(125, 95, 50);
    }

    private float[] rgb(int r, int g, int b) {
        return new float[] {
                r / 255.0F,
                g / 255.0F,
                b / 255.0F
        };
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
                           float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ,
                           int light) {

        PoseStack.Pose pose = poseStack.last();

        vertex(consumer, pose, minX, minY, minZ, 0, 0, 0, 0, -1, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, minY, minZ, 1, 0, 0, 0, -1, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, maxY, minZ, 1, 1, 0, 0, -1, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, maxY, minZ, 0, 1, 0, 0, -1, light, 1, 1, 1, 1);

        vertex(consumer, pose, minX, minY, maxZ, 0, 0, 0, 0, 1, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, maxY, maxZ, 0, 1, 0, 0, 1, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, maxY, maxZ, 1, 1, 0, 0, 1, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, minY, maxZ, 1, 0, 0, 0, 1, light, 1, 1, 1, 1);

        vertex(consumer, pose, minX, minY, minZ, 0, 0, -1, 0, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, maxY, minZ, 0, 1, -1, 0, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, maxY, maxZ, 1, 1, -1, 0, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, minY, maxZ, 1, 0, -1, 0, 0, light, 1, 1, 1, 1);

        vertex(consumer, pose, maxX, minY, minZ, 0, 0, 1, 0, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, minY, maxZ, 1, 0, 1, 0, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, maxY, maxZ, 1, 1, 1, 0, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, maxY, minZ, 0, 1, 1, 0, 0, light, 1, 1, 1, 1);

        vertex(consumer, pose, minX, minY, minZ, 0, 0, 0, -1, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, minY, maxZ, 0, 1, 0, -1, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, minY, maxZ, 1, 1, 0, -1, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, minY, minZ, 1, 0, 0, -1, 0, light, 1, 1, 1, 1);

        vertex(consumer, pose, minX, maxY, minZ, 0, 0, 0, 1, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, maxY, minZ, 1, 0, 0, 1, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, maxX, maxY, maxZ, 1, 1, 0, 1, 0, light, 1, 1, 1, 1);
        vertex(consumer, pose, minX, maxY, maxZ, 0, 1, 0, 1, 0, light, 1, 1, 1, 1);
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                        float x, float y, float z,
                        float u, float v,
                        float nx, float ny, float nz,
                        int light,
                        float r, float g, float b, float a) {

        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}