package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.neoforged.fml.ModList;

/**
 * Loader-safe entry point for all Create integration.
 *
 * <p>This class deliberately contains no imports or signatures from Create. Common Tobacconist
 * code may safely reference this class even when Create is not installed. Classes that directly
 * reference Create APIs are loaded reflectively only after {@link #loaded()} is true.</p>
 */
public final class CreateCompat {
    public static final String MOD_ID = "create";

    private static final String DEPLOYER_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateDeployerCompat";

    private CreateCompat() {}

    public static boolean loaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init() {
        if (!loaded()) {
            TobacconistMod.LOGGER.debug("Create not detected; Create compatibility remains disabled.");
            return;
        }

        registerCreateIntegration(DEPLOYER_COMPAT_CLASS);
        TobacconistMod.LOGGER.info("Create detected; Tobacconist Create compatibility enabled.");
    }

    private static void registerCreateIntegration(String className) {
        try {
            Class<?> integrationClass = Class.forName(className);
            integrationClass.getMethod("register").invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Create is installed, but Tobacconist Create compatibility failed to initialize: " + className,
                    exception
            );
        }
    }
}
