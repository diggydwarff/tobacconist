package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.screen.FlueFireboxScreen;
import com.diggydwarff.tobacconistmod.screen.HookahScreen;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.HOOKAH_MENU.get(), HookahScreen::new);
        event.register(ModMenuTypes.FLUE_FIREBOX_MENU.get(), FlueFireboxScreen::new);
    }
}