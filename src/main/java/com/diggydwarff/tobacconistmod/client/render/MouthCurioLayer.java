package com.diggydwarff.tobacconistmod.client.render;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.CigarItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.CigaretteItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.WoodenSmokingPipeItem;
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
import top.theillusivec4.curios.api.CuriosApi;

public class MouthCurioLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final ItemRenderer itemRenderer;

    public MouthCurioLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, ItemRenderer renderer) {
        super(parent);
        this.itemRenderer = renderer;
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
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {

            var handler = inv.getCurios().get("mouth");

            if (handler != null) {

                ItemStack stack = handler.getStacks().getStackInSlot(0);

                if (!stack.isEmpty()) {
                    poseStack.pushPose();
                    this.getParentModel().head.translateAndRotate(poseStack);

                    if (stack.getItem() instanceof WoodenSmokingPipeItem) {
                        float x = 0.175F;
                        float y = 0.000F;
                        float z = -0.30F;

                        // The Emerald-Aztec pipe and Kiseru sit visually lower in the mouth slot because
                        // their sprites have more empty/weighted pixels below the stem. Lift them slightly
                        // so the mouthpiece actually reaches the lips like the other smoking items.
                        if (stack.is(ModItems.EMERALD_AZTEC_SMOKING_PIPE.get())) {
                            y = -0.070F;
                        } else if (stack.is(ModItems.KISERU_SMOKING_PIPE.get())) {
                            y = -0.085F;
                        }

                        poseStack.translate(x, y, z);

                        poseStack.mulPose(Axis.XP.rotationDegrees(90F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(65F)); // Pull out from flat a bit towards viewer
                        poseStack.mulPose(Axis.YP.rotationDegrees(-90F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));

                        //poseStack.scale(0.42F, 0.42F, 0.42F);
                        poseStack.scale(0.30F, 0.30F, 0.30F);  // smaller
                    } else if (stack.getItem() instanceof CigarItem || stack.getItem() instanceof CigaretteItem) {
                        poseStack.translate(0.100F, -0.050F, -0.28F);

                        poseStack.mulPose(Axis.XP.rotationDegrees(90F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(55F)); // Pull out from flat a bit towards viewer
                        poseStack.mulPose(Axis.YP.rotationDegrees(-90F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));

                        //poseStack.scale(0.42F, 0.42F, 0.42F);
                        poseStack.scale(0.20F, 0.20F, 0.20F);  // even smaller
                    }

                    itemRenderer.renderStatic(
                            stack,
                            ItemDisplayContext.FIXED,
                            packedLight,
                            0,
                            poseStack,
                            buffer,
                            player.level(),
                            0
                    );

                    poseStack.popPose();
                }
            }
        });
    }
}