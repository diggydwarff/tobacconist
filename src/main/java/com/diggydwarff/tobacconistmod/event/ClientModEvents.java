package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.custom.ProductionMonitorBlock;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.renderer.TobaccoDryingRackRenderer;
import com.diggydwarff.tobacconistmod.block.entity.renderer.ProductionMonitorRenderer;
import com.diggydwarff.tobacconistmod.client.TobaccoInspectionOverlay;
import com.diggydwarff.tobacconistmod.client.particle.TobaccoFanSmokeParticle;
import com.diggydwarff.tobacconistmod.particle.ModParticles;
import net.neoforged.api.distmarker.Dist;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerInspectionOverlay(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "spectacles_inspection"),
                TobaccoInspectionOverlay::render
        );
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ModParticles.TOBACCO_SMOKE.get(),
                sprites -> new TobaccoFanSmokeParticle.Provider(sprites, false)
        );
        event.registerSpriteSet(
                ModParticles.TOBACCO_INDOOR_SMOKE.get(),
                sprites -> new TobaccoFanSmokeParticle.Provider(sprites, true)
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.TOBACCO_DRYING_RACK.get(),
                TobaccoDryingRackRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.PRODUCTION_MONITOR.get(),
                ProductionMonitorRenderer::new
        );
    }
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
        double min = 0.25D;
        double max = 0.75D;
        double near = 0.5D / 16.0D;
        double far = 15.5D / 16.0D;
        double thickness = 1.0D / 512.0D;
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
            float dx = (float) (x2 - x1);
            float dy = (float) (y2 - y1);
            float dz = (float) (z2 - z1);
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length <= 0.0F) return;
            dx /= length;
            dy /= length;
            dz /= length;
            lines.addVertex(pose.pose(), (float) x1, (float) y1, (float) z1)
                    .setColor(1.0F, 1.0F, 1.0F, 0.85F)
                    .setNormal(pose.copy(), dx, dy, dz);
            lines.addVertex(pose.pose(), (float) x2, (float) y2, (float) z2)
                    .setColor(1.0F, 1.0F, 1.0F, 0.85F)
                    .setNormal(pose.copy(), dx, dy, dz);
        });
        poseStack.popPose();
    }

}