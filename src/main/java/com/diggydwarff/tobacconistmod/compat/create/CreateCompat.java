package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Loader-safe entry point for optional Create integration. Create API classes are loaded only
 * after {@link #loaded()} succeeds, so common code can reference this class without Create.
 */
public final class CreateCompat {
    public static final String MOD_ID = "create";

    private static final String DEPLOYER_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateDeployerCompat";
    private static final String PRESS_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreatePressCompat";
    private static final String MIXER_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateMixerCompat";
    private static final String FILLING_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateFillingCompat";
    private static final String FAN_CURING_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateFanCuringCompat";
    private static final String DISPLAY_LINK_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateDisplayLinkCompat";
    private static final String ITEM_ATTRIBUTE_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateItemAttributeCompat";
    private static final String ARM_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateArmCompat";
    private static final String LOGISTICS_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateLogisticsCompat";
    private static final String SMOKE_CLEARING_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateSmokeClearingCompat";
    private static final String PONDER_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreatePonderCompat";
    private static final String HOMOGENIZATION_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateHomogenizationCompat";
    private static final String PRODUCTION_MONITOR_COMPAT_CLASS =
            "com.diggydwarff.tobacconistmod.compat.create.CreateProductionMonitorCompat";

    private static BiFunction<Level, BlockPos, FanCuringAssist> fanCuringResolver =
            (level, pos) -> FanCuringAssist.NONE;
    private static BiFunction<Level, Vec3, SmokeAirflow> smokeAirflowResolver =
            (level, pos) -> SmokeAirflow.NONE;
    private static BiFunction<Level, BlockPos, HomogenizationStatus> homogenizationStatusResolver =
            (level, pos) -> HomogenizationStatus.NONE;
    private static ProductionMonitorBridge productionMonitorBridge = ProductionMonitorBridge.NONE;

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
        registerCreateIntegration(FILLING_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(FAN_CURING_COMPAT_CLASS);
        registerCreateIntegration(DISPLAY_LINK_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(ITEM_ATTRIBUTE_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(ARM_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(LOGISTICS_COMPAT_CLASS, modEventBus);
        registerCreateIntegration(SMOKE_CLEARING_COMPAT_CLASS);
        registerCreateIntegration(HOMOGENIZATION_COMPAT_CLASS);
        registerCreateIntegration(PRODUCTION_MONITOR_COMPAT_CLASS);
        TobacconistMod.LOGGER.info("Create detected; Tobacconist Create compatibility enabled.");
    }

    /** Client-only Create/Ponder setup, kept behind reflection so Create remains optional. */
    public static void initClient() {
        if (!loaded()) {
            return;
        }
        registerCreateIntegration(PONDER_COMPAT_CLASS);
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

    /** Compatibility alias for common-side callers. */
    public static boolean isSmokeVentilated(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        return getSmokeAirflow(level, Vec3.atCenterOf(pos)).active();
    }

    static void installSmokeAirflowResolver(BiFunction<Level, Vec3, SmokeAirflow> resolver) {
        smokeAirflowResolver = Objects.requireNonNull(resolver);
    }

    public record HomogenizationStatus(boolean relevant, int count, int target, double averageQuality,
                                       int predictedQuality, boolean ready, int signalStrength,
                                       boolean processing, boolean finishMode, boolean finishArmed,
                                       int incompatibleCount, boolean uniform) {
        public static final HomogenizationStatus NONE =
                new HomogenizationStatus(false, 0, 64, 0.0D, 0, false, 0,
                        false, false, false, 0, false);
    }

    public static HomogenizationStatus getHomogenizationStatus(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return HomogenizationStatus.NONE;
        }
        HomogenizationStatus status = homogenizationStatusResolver.apply(level, pos);
        return status == null ? HomogenizationStatus.NONE : status;
    }

    static void installHomogenizationStatusResolver(
            BiFunction<Level, BlockPos, HomogenizationStatus> resolver) {
        homogenizationStatusResolver = Objects.requireNonNull(resolver);
    }

    public interface ProductionMonitorBridge {
        ProductionMonitorBridge NONE = new ProductionMonitorBridge() {};

        default boolean isSupportedTarget(Level level, BlockPos pos) { return false; }
        default void observe(ProductionMonitorBlockEntity monitor, BlockPos targetPos) {}
        default boolean isCreateFilter(ItemStack filter) { return false; }
        default boolean matchesFilter(Level level, ItemStack filter, ItemStack candidate) { return false; }
    }

    public static boolean isProductionMonitorTarget(Level level, BlockPos pos) {
        return loaded() && level != null && pos != null && productionMonitorBridge.isSupportedTarget(level, pos);
    }

    public static void observeProductionMonitor(ProductionMonitorBlockEntity monitor, BlockPos targetPos) {
        if (loaded() && monitor != null && targetPos != null) productionMonitorBridge.observe(monitor, targetPos);
    }

    public static boolean isCreateProductionMonitorFilter(ItemStack filter) {
        return loaded() && filter != null && !filter.isEmpty() && productionMonitorBridge.isCreateFilter(filter);
    }

    public static boolean matchesProductionMonitorFilter(Level level, ItemStack filter, ItemStack candidate) {
        return loaded() && productionMonitorBridge.matchesFilter(level, filter, candidate);
    }

    static void installProductionMonitorBridge(ProductionMonitorBridge bridge) {
        productionMonitorBridge = Objects.requireNonNull(bridge);
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
