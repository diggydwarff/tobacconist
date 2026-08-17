package com.diggydwarff.tobacconistmod.client.render;

import com.diggydwarff.tobacconistmod.compat.SpectaclesEquipmentHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SpectaclesRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ItemRenderer itemRenderer;
    public SpectaclesRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, ItemRenderer itemRenderer) {
        super(parent); this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack spectacles = SpectaclesEquipmentHelper.findWorn(player);
        if (spectacles.isEmpty()) return;
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.40F, -0.255F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.52F, 0.52F, 0.52F);
        itemRenderer.renderStatic(spectacles, ItemDisplayContext.FIXED, packedLight, 0,
                poseStack, buffer, player.level(), player.getId());
        poseStack.popPose();
    }
}
