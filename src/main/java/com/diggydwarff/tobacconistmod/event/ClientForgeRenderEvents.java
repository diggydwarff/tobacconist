package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.ProductionMonitorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge-bus client rendering hooks separated from MOD-bus registration events. */
@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public final class ClientForgeRenderEvents {
    private ClientForgeRenderEvents() {}

    @SubscribeEvent
    public static void renderProductionMonitorFilterHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.player.isSpectator()) return;

        BlockHitResult hit = event.getTarget();
        BlockPos pos = hit.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        if (!state.is(ModBlocks.PRODUCTION_MONITOR.get())) return;
        if (!ProductionMonitorBlock.isExternalFilterSlotHit(state, pos, hit)) return;

        Direction operatorFace = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        double min = 0.25D, max = 0.75D;
        double near = 0.5D / 16.0D, far = 15.5D / 16.0D, thickness = 1.0D / 512.0D;
        AABB highlight = switch (operatorFace) {
            case NORTH -> new AABB(min, min, near - thickness, max, max, near + thickness);
            case SOUTH -> new AABB(min, min, far - thickness, max, max, far + thickness);
            case WEST -> new AABB(near - thickness, min, min, near + thickness, max, max);
            case EAST -> new AABB(far - thickness, min, min, far + thickness, max, max);
            default -> null;
        };
        if (highlight == null) return;

        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        PoseStack.Pose pose = poseStack.last();
        Shapes.create(highlight).forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length <= 0.0F) return;
            dx /= length; dy /= length; dz /= length;
            lines.vertex(pose.pose(), (float) x1, (float) y1, (float) z1)
                    .color(1.0F, 1.0F, 1.0F, 0.85F).normal(pose.normal(), dx, dy, dz).endVertex();
            lines.vertex(pose.pose(), (float) x2, (float) y2, (float) z2)
                    .color(1.0F, 1.0F, 1.0F, 0.85F).normal(pose.normal(), dx, dy, dz).endVertex();
        });
        poseStack.popPose();
    }
}
