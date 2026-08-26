package com.diggydwarff.tobacconistmod.block.entity.renderer;

import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Renders the Production Monitor ghost filter using Create's centered value-box proportions. */
public class ProductionMonitorRenderer implements BlockEntityRenderer<ProductionMonitorBlockEntity> {
    private static final double VALUE_BOX_PLANE = 15.5D / 16.0D;
    private static final double OPPOSITE_PLANE = 0.5D / 16.0D;
    private static final float VALUE_BOX_SCALE = 0.5F;

    private final ItemRenderer itemRenderer;

    public ProductionMonitorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ProductionMonitorBlockEntity monitor, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack filter = monitor.getFilter();
        if (filter.isEmpty()) return;

        Direction monitoredDirection = monitor.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction operatorFace = monitoredDirection.getOpposite();

        poseStack.pushPose();
        switch (operatorFace) {
            case NORTH -> poseStack.translate(0.5D, 0.5D, OPPOSITE_PLANE);
            case SOUTH -> poseStack.translate(0.5D, 0.5D, VALUE_BOX_PLANE);
            case WEST -> poseStack.translate(OPPOSITE_PLANE, 0.5D, 0.5D);
            case EAST -> poseStack.translate(VALUE_BOX_PLANE, 0.5D, 0.5D);
            default -> {
                poseStack.popPose();
                return;
            }
        }

        // Mirrors Create ValueBoxTransform.Sided + ValueBoxRenderer.renderItemIntoValueBox.
        poseStack.mulPose(Axis.YP.rotationDegrees(operatorFace.toYRot() + 180.0F));
        poseStack.scale(VALUE_BOX_SCALE, VALUE_BOX_SCALE, VALUE_BOX_SCALE);

        BakedModel model = itemRenderer.getModel(filter, null, null, 0);
        boolean blockItem = model.isGui3d();
        float itemScale = (!blockItem ? 0.5F : 1.0F) + 1.0F / 64.0F;
        float zOffset = !blockItem ? -0.15F : 0.0F;
        poseStack.scale(itemScale, itemScale, itemScale);
        poseStack.translate(0.0D, 0.0D, zOffset);
        // The monitor itself is a solid cube, so the block entity light passed into this renderer
        // can be sampled from inside the block and make the externally mounted ghost item look
        // unnaturally dark. Sample the air immediately in front of the operator face instead,
        // matching how an externally attached Create value-box/filter is lit in the world.
        int filterLight = packedLight;
        if (monitor.getLevel() != null) {
            filterLight = LevelRenderer.getLightColor(
                    monitor.getLevel(), monitor.getBlockPos().relative(operatorFace));
        }
        itemRenderer.render(filter, ItemDisplayContext.FIXED, false, poseStack, buffer, filterLight, packedOverlay, model);
        poseStack.popPose();
    }
}
