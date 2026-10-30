package com.diggydwarff.tobacconistmod.client.render;

import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.CigarItem;
import com.diggydwarff.tobacconistmod.datagen.items.custom.CigaretteItem;
import com.diggydwarff.tobacconistmod.novelty.RelicPipeItem;
import com.diggydwarff.tobacconistmod.novelty.ToyPipeItem;
import com.diggydwarff.tobacconistmod.vapor.BubblePipeItem;
import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
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

/** One player layer for every mouth item. Curios stores the equipment; this layer draws it. */
public final class MouthCurioLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ItemRenderer itemRenderer;

    /** Explicit mod-bus registration keeps mouth rendering independent of subscriber bus inference. */
    public static void register(net.minecraftforge.eventbus.api.IEventBus modEventBus) {
        modEventBus.addListener(MouthCurioLayer::addLayers);
    }

    private static void addLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            net.minecraft.client.renderer.entity.player.PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new MouthCurioLayer(
                    renderer, net.minecraft.client.Minecraft.getInstance().getItemRenderer()));
        }
    }

    public MouthCurioLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                           ItemRenderer itemRenderer) {
        super(parent);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                       float limbSwing, float limbAmount, float partialTick, float age,
                       float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        ItemStack stack = CuriosSmokingHelper.visible(player).stack();
        if (stack.isEmpty()) return;

        pose.pushPose();
        try {
            getParentModel().head.translateAndRotate(pose);
            applyPose(stack, pose);
            itemRenderer.renderStatic(player, stack, TransformType.FIXED, false,
                    pose, buffer, player.level, light, 0, player.getId());
        } finally {
            pose.popPose();
        }
    }

    public static void applyPose(ItemStack stack, PoseStack pose) {
        if (VapePenItem.isCigalike(stack)) MouthSpritePose.CIGALIKE.apply(pose);
        else if (stack.getItem() instanceof BubblePipeItem) MouthSpritePose.BUBBLE_PIPE.apply(pose);
        else if (stack.is(com.diggydwarff.tobacconistmod.novelty.NoveltyContent.PETAL_PIPE.get())) MouthSpritePose.PETAL_PIPE.apply(pose);
        else if (stack.getItem() instanceof RelicPipeItem || stack.getItem() instanceof ToyPipeItem) {
            MouthSpritePose.SPECIAL_PIPE.apply(pose);
        } else if (stack.getItem() instanceof CigarItem || stack.getItem() instanceof CigaretteItem) {
            legacyPose(pose, .100F, -.050F, -.28F, 55F, .20F);
        } else {
            float y=stack.is(ModItems.EMERALD_AZTEC_SMOKING_PIPE.get()) ? -.070F
                    : stack.is(ModItems.KISERU_SMOKING_PIPE.get()) ? -.085F : 0F;
            legacyPose(pose, .175F, y, -.30F, 65F, .30F);
        }
    }

    private static void legacyPose(PoseStack pose, float x, float y, float z, float angle, float size) {
        pose.translate(x,y,z);
        pose.mulPose(Vector3f.XP.rotationDegrees(90F));
        pose.mulPose(Vector3f.ZP.rotationDegrees(angle));
        pose.mulPose(Vector3f.YP.rotationDegrees(-90F));
        pose.mulPose(Vector3f.ZP.rotationDegrees(-90F));
        pose.scale(size,size,size);
    }
}
