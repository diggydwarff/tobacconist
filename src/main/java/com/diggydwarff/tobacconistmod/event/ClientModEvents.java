package com.diggydwarff.tobacconistmod.event;

import net.minecraftforge.fml.common.Mod;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.renderer.TobaccoDryingRackRenderer;
import com.diggydwarff.tobacconistmod.block.entity.renderer.ProductionMonitorRenderer;
import com.diggydwarff.tobacconistmod.client.TobaccoInspectionOverlay;
import com.diggydwarff.tobacconistmod.client.particle.TobaccoFanSmokeParticle;
import com.diggydwarff.tobacconistmod.particle.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerInspectionOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("spectacles_inspection", TobaccoInspectionOverlay::render);
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


}
