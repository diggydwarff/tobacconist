package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.ProductionMonitorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/** Client game-bus hover feedback, including when the ghost filter is empty. */
@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ProductionMonitorHighlightEvents {
    private ProductionMonitorHighlightEvents() {}

    @SubscribeEvent
    public static void renderFilterHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.player.isSpectator()) return;
        BlockHitResult hit = event.getTarget();
        BlockPos pos = hit.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        if (!state.is(ModBlocks.PRODUCTION_MONITOR.get())
                || !ProductionMonitorBlock.isExternalFilterSlotHit(state, pos, hit)) return;

        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        PoseStack.Pose pose = poseStack.last();
        Shapes.create(ProductionMonitorBlock.getExternalFilterSlotOutline(state)).forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float dx = (float) (x2 - x1);
            float dy = (float) (y2 - y1);
            float dz = (float) (z2 - z1);
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length <= 0.0F) return;
            dx /= length;
            dy /= length;
            dz /= length;
            lines.addVertex(pose.pose(), (float) x1, (float) y1, (float) z1)
                    .setColor(1.0F, 1.0F, 1.0F, 1.0F).setNormal(pose.copy(), dx, dy, dz);
            lines.addVertex(pose.pose(), (float) x2, (float) y2, (float) z2)
                    .setColor(1.0F, 1.0F, 1.0F, 1.0F).setNormal(pose.copy(), dx, dy, dz);
        });
        poseStack.popPose();
    }
}
