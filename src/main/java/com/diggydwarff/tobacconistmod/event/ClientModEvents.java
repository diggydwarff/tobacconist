package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.renderer.TobaccoDryingRackRenderer;
import com.diggydwarff.tobacconistmod.client.TobaccoInspectionOverlay;
import com.diggydwarff.tobacconistmod.client.particle.TobaccoFanSmokeParticle;
import com.diggydwarff.tobacconistmod.particle.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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
    }
}