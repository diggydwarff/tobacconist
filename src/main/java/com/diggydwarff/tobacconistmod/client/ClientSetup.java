package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraftforge.fml.ModList;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final KeyMapping USE_AS_CURIO = new KeyMapping(
      "key.tobacconistmod.use_curio",
      InputConstants.Type.KEYSYM,
      InputConstants.KEY_R,
      "key.categories.tobacconistmod"
    );

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ORNATE_DIAMOND_HOOKAH.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ORNATE_AMETHYST_HOOKAH.get(), RenderType.translucent());
        });
    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
      if (!ModList.get().isLoaded("curios")) return;
      event.register(USE_AS_CURIO);
    }
}
