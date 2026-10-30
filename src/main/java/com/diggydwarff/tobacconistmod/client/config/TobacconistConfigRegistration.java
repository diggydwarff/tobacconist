package com.diggydwarff.tobacconistmod.client.config;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Keep GUI registration strictly on the physical client and mod lifecycle bus. */
@EventBusSubscriber(
    modid = TobacconistMod.MODID,
    bus = EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public final class TobacconistConfigRegistration {
  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    ModList.get()
        .getModContainerById(TobacconistMod.MODID)
        .orElseThrow()
        .registerExtensionPoint(IConfigScreenFactory.class, TobacconistConfigScreen::new);
  }

  private TobacconistConfigRegistration() {}
}
