package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import java.util.Objects;
import java.util.function.BiFunction;

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
    private static final String PRESS_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreatePressCompat";
    private static final String MIXER_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateMixerCompat";
    private static final String FAN_CURING_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateFanCuringCompat";
    private static final String DISPLAY_LINK_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateDisplayLinkCompat";
    private static final String ITEM_ATTRIBUTE_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateItemAttributeCompat";
    private static final String SMOKE_CLEARING_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateSmokeClearingCompat";

    private static BiFunction<Level, BlockPos, FanCuringAssist> fanCuringResolver =
            (level, pos) -> FanCuringAssist.NONE;
    private static BiFunction<Level, Vec3, SmokeAirflow> smokeAirflowResolver =
            (level, pos) -> SmokeAirflow.NONE;

    private CreateCompat() {}

    public static boolean loaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init(IEventBus modEventBus) {
        if (!loaded()) {
            TobacconistMod.LOGGER.debug("Create not detected; Create compatibility remains disabled.");
            return;
        }

        registerCreateIntegration(DEPLOYER_COMPAT_CLASS);
        registerCreateIntegration(PRESS_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(MIXER_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(FAN_CURING_COMPAT_CLASS);
        registerCreateIntegration(DISPLAY_LINK_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(ITEM_ATTRIBUTE_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(SMOKE_CLEARING_COMPAT_CLASS);
        TobacconistMod.LOGGER.info("Create detected; Tobacconist Create compatibility enabled.");
    }

    /**
     * Loader-safe description of Create airflow reaching a Tobacconist Drying Rack.
     * No Create classes appear in this API, so common Tobacconist code can call it safely when
     * Create is absent.
     */
    public enum FanCuringAssist {
        NONE(0),
        AIR(1),
        FLUE(2),
        FIRE(3);

        private final int priority;

        FanCuringAssist(int priority) {
            this.priority = priority;
        }

        public int priority() {
            return priority;
        }
    }


    /**
     * Loader-safe description of smoke movement contributed by optional integrations.
     * The direction is normalized; strength is a small per-particle velocity contribution.
     */
    public record SmokeAirflow(double x, double y, double z, double strength, boolean intakeCapture) {
        public static final SmokeAirflow NONE = new SmokeAirflow(0.0D, 0.0D, 0.0D, 0.0D, false);

        public boolean active() {
            return strength > 0.0D && (x != 0.0D || y != 0.0D || z != 0.0D);
        }
    }

    public static FanCuringAssist getFanCuringAssist(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return FanCuringAssist.NONE;
        }
        return fanCuringResolver.apply(level, pos);
    }

    static void installFanCuringResolver(BiFunction<Level, BlockPos, FanCuringAssist> resolver) {
        fanCuringResolver = Objects.requireNonNull(resolver);
    }

    public static SmokeAirflow getSmokeAirflow(Level level, Vec3 pos) {
        if (level == null || pos == null) {
            return SmokeAirflow.NONE;
        }
        SmokeAirflow airflow = smokeAirflowResolver.apply(level, pos);
        return airflow == null ? SmokeAirflow.NONE : airflow;
    }

    /** Kept as a convenience for any older common-side callers. */
    public static boolean isSmokeVentilated(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        return getSmokeAirflow(level, Vec3.atCenterOf(pos)).active();
    }

    static void installSmokeAirflowResolver(BiFunction<Level, Vec3, SmokeAirflow> resolver) {
        smokeAirflowResolver = Objects.requireNonNull(resolver);
    }

    private static void registerCreateIntegration(String className, IEventBus modEventBus) {
        try {
            Class<?> integrationClass = Class.forName(className);
            integrationClass.getMethod("register", IEventBus.class).invoke(null, modEventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Create is installed, but Tobacconist Create compatibility failed to initialize: " + className,
                    exception
            );
        }
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
