package com.diggydwarff.tobacconistmod.client.render;

import com.diggydwarff.tobacconistmod.compat.SpectaclesEquipmentHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.world.item.ItemStack;

/** Renders the spectacles in the same forehead position for both vanilla HEAD and Curios Head equipment. */
public class SpectaclesRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ItemRenderer itemRenderer;

    public SpectaclesRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            ItemRenderer itemRenderer
    ) {
        super(parent);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ItemStack spectacles = SpectaclesEquipmentHelper.findVisibleWorn(player);
        if (spectacles.isEmpty()) return;

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);

        // Position the item model on the forehead.
        poseStack.translate(0.0F, -0.40F, -0.255F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(0.52F, 0.52F, 0.52F);

        itemRenderer.renderStatic(player, spectacles, TransformType.FIXED, false,
                poseStack, buffer, player.level, packedLight, 0, player.getId());

        poseStack.popPose();
    }
}
