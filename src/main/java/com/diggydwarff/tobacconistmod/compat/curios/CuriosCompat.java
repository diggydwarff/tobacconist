package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

/** Loader-safe Curios bootstrap. Curios classes are only resolved after the mod is confirmed loaded. */
public final class CuriosCompat {
    private static final String SPECTACLES_REGISTRATION_CLASS =
            "com.diggydwarff.tobacconistmod.compat.curios.CuriosSpectaclesRegistration";

    private CuriosCompat() {}

    public static boolean loaded() {
        return ModList.get().isLoaded("curios");
    }

    public static void register(IEventBus modEventBus) {
        if (!loaded()) return;

        try {
            Class<?> registrationClass = Class.forName(SPECTACLES_REGISTRATION_CLASS);
            registrationClass.getMethod("register", IEventBus.class).invoke(null, modEventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Curios is installed, but Tobacconist spectacles compatibility failed to initialize.",
                    exception
            );
        }

        TobacconistMod.LOGGER.info("Curios compatibility enabled.");
    }
}
