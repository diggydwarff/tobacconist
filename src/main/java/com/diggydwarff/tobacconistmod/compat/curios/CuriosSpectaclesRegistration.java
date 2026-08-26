package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Curios-only registration, loaded reflectively so Curios remains an optional dependency. */
public final class CuriosSpectaclesRegistration {
    private CuriosSpectaclesRegistration() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CuriosSpectaclesRegistration::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // The #curios:head tag assigns the slot; the explicit capability matches Curios' supported API.
            CuriosApi.registerCurio(ModItems.TOBACCONISTS_SPECTACLES.get(), new ICurioItem() {});
            TobacconistMod.LOGGER.info("Curios Head-slot support enabled for Tobacconist's Spectacles.");
        });
    }
}
