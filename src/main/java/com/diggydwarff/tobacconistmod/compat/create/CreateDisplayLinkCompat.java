package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.FlueFireboxBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Create Display Link sources for Tobacconist processing blocks. */
public final class CreateDisplayLinkCompat {
    private static final DeferredRegister<DisplaySource> DISPLAY_SOURCES =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, TobacconistMod.MODID);

    private static final DeferredHolder<DisplaySource, DryingRackStatusSource> DRYING_RACK_STATUS =
            DISPLAY_SOURCES.register("drying_rack_status", DryingRackStatusSource::new);
    private static final DeferredHolder<DisplaySource, DryingRackProgressSource> DRYING_RACK_PROGRESS =
            DISPLAY_SOURCES.register("drying_rack_progress", DryingRackProgressSource::new);
    private static final DeferredHolder<DisplaySource, DryingRackLeafCountSource> DRYING_RACK_LEAF_COUNT =
            DISPLAY_SOURCES.register("drying_rack_leaf_count", DryingRackLeafCountSource::new);
    private static final DeferredHolder<DisplaySource, BarrelStatusSource> BARREL_STATUS =
            DISPLAY_SOURCES.register("tobacco_barrel_status", BarrelStatusSource::new);
    private static final DeferredHolder<DisplaySource, BarrelProgressSource> BARREL_PROGRESS =
            DISPLAY_SOURCES.register("tobacco_barrel_progress", BarrelProgressSource::new);
    private static final DeferredHolder<DisplaySource, BarrelHumiditySource> BARREL_HUMIDITY =
            DISPLAY_SOURCES.register("tobacco_barrel_humidity", BarrelHumiditySource::new);
    private static final DeferredHolder<DisplaySource, BarrelAgeSource> BARREL_AGE =
            DISPLAY_SOURCES.register("tobacco_barrel_age", BarrelAgeSource::new);
    private static final DeferredHolder<DisplaySource, FlueFireboxStatusSource> FLUE_FIREBOX_STATUS =
            DISPLAY_SOURCES.register("flue_firebox_status", FlueFireboxStatusSource::new);
    private static final DeferredHolder<DisplaySource, FlueFireboxFuelSource> FLUE_FIREBOX_FUEL =
            DISPLAY_SOURCES.register("flue_firebox_fuel", FlueFireboxFuelSource::new);
    private static final DeferredHolder<DisplaySource, HookahStatusSource> HOOKAH_STATUS =
            DISPLAY_SOURCES.register("hookah_status", HookahStatusSource::new);
    private static final DeferredHolder<DisplaySource, HomogenizationStatusSource> HOMOGENIZATION_STATUS =
            DISPLAY_SOURCES.register("homogenization_status", HomogenizationStatusSource::new);
    private static final DeferredHolder<DisplaySource, HomogenizationAverageSource> HOMOGENIZATION_AVERAGE =
            DISPLAY_SOURCES.register("homogenization_average", HomogenizationAverageSource::new);
    private static final DeferredHolder<DisplaySource, VaultTobaccoCountSource> VAULT_TOBACCO_COUNT =
            DISPLAY_SOURCES.register("vault_tobacco_count", VaultTobaccoCountSource::new);
    private static final DeferredHolder<DisplaySource, VaultAverageQualitySource> VAULT_AVERAGE_QUALITY =
            DISPLAY_SOURCES.register("vault_average_quality", VaultAverageQualitySource::new);

    private CreateDisplayLinkCompat() {}

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
        modEventBus.addListener(CreateDisplayLinkCompat::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_DRYING_RACK.get(), DRYING_RACK_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_DRYING_RACK.get(), DRYING_RACK_PROGRESS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_DRYING_RACK.get(), DRYING_RACK_LEAF_COUNT.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(), DRYING_RACK_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(), DRYING_RACK_PROGRESS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.INDUSTRIAL_DRYING_RACK.get(), DRYING_RACK_LEAF_COUNT.get());

            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_PROGRESS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_HUMIDITY.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_AGE.get());

            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FLUE_FIREBOX.get(), FLUE_FIREBOX_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FLUE_FIREBOX.get(), FLUE_FIREBOX_FUEL.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.HOOKAH.get(), HOOKAH_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(AllBlockEntityTypes.BASIN.get(), HOMOGENIZATION_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(AllBlockEntityTypes.BASIN.get(), HOMOGENIZATION_AVERAGE.get());
            DisplaySource.BY_BLOCK_ENTITY.add(AllBlockEntityTypes.ITEM_VAULT.get(), VAULT_TOBACCO_COUNT.get());
            DisplaySource.BY_BLOCK_ENTITY.add(AllBlockEntityTypes.ITEM_VAULT.get(), VAULT_AVERAGE_QUALITY.get());
        });
    }

    private abstract static class TobacconistSingleLineSource extends SingleLineDisplaySource {
        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

        @Override
        public int getPassiveRefreshTicks() {
            return 20;
        }

        protected <T extends BlockEntity> T source(DisplayLinkContext context, Class<T> type) {
            BlockEntity blockEntity = context.getSourceBlockEntity();
            return type.isInstance(blockEntity) ? type.cast(blockEntity) : null;
        }
    }

    private static final class DryingRackStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoDryingRackBlockEntity rack = source(context, TobaccoDryingRackBlockEntity.class);
            return rack == null ? EMPTY_LINE : rack.getRackStatusComponent();
        }
    }

    private static final class DryingRackProgressSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoDryingRackBlockEntity rack = source(context, TobaccoDryingRackBlockEntity.class);
            return rack == null ? EMPTY_LINE : Component.translatable("tobacconistmod.ui.percent", rack.getDryProgressPercent());
        }
    }

    private static final class DryingRackLeafCountSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoDryingRackBlockEntity rack = source(context, TobaccoDryingRackBlockEntity.class);
            return rack == null ? EMPTY_LINE : Component.translatable("tobacconistmod.ui.leaf_count_short", rack.getLeafCount(), rack.getMaxLeaves());
        }
    }

    private static final class BarrelStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            if (barrel == null) return EMPTY_LINE;
            if (barrel.getStoredTobacco().isEmpty()) return Component.translatable("tobacconistmod.ui.empty");

            MutableComponent mode = TobaccoText.barrelMode(barrel.getMode());
            int progress = barrel.getProcessProgressPercent();
            if (progress > 0) {
                return Component.translatable("tobacconistmod.status.method_progress", mode, progress);
            }
            return mode;
        }
    }

    private static final class BarrelProgressSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            return barrel == null ? EMPTY_LINE : Component.translatable("tobacconistmod.ui.percent", barrel.getProcessProgressPercent());
        }
    }

    private static final class BarrelHumiditySource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            return barrel == null ? EMPTY_LINE : Component.translatable("tobacconistmod.ui.percent", barrel.getBarrelHumidity());
        }
    }

    private static final class BarrelAgeSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            if (barrel == null) return EMPTY_LINE;

            ItemStack stored = barrel.getStoredTobacco();
            if (stored.isEmpty()) return TobaccoText.ageDuration(0);

            int days = TobaccoBarrelBlockEntity.getAgedDays(stored);
            return TobaccoText.ageDuration(days);
        }
    }

    private static final class FlueFireboxStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            FlueFireboxBlockEntity firebox = source(context, FlueFireboxBlockEntity.class);
            return firebox == null ? EMPTY_LINE : Component.translatable(firebox.isLit() ? "tobacconistmod.ui.lit" : "tobacconistmod.ui.idle");
        }
    }

    private static final class FlueFireboxFuelSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            FlueFireboxBlockEntity firebox = source(context, FlueFireboxBlockEntity.class);
            if (firebox == null) return EMPTY_LINE;
            int total = firebox.getBurnTimeTotal();
            int percent = total <= 0 ? 0 : Math.min(100, firebox.getBurnTime() * 100 / total);
            return Component.translatable("tobacconistmod.ui.percent", percent);
        }
    }

    private static final class HomogenizationStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            BasinBlockEntity basin = source(context, BasinBlockEntity.class);
            if (basin == null || basin.getLevel() == null) return EMPTY_LINE;

            CreateTobaccoHomogenization.HomogenizationStatus status =
                    CreateTobaccoHomogenization.getStatus(basin.getLevel(), basin.getBlockPos());
            if (!status.relevant()) return Component.translatable("tobacconistmod.ui.no_tobacco_batch");
            if (status.processing()) {
                return Component.translatable("tobacconistmod.display.homogenizing_batch", status.count(), status.target());
            }
            if (status.finishMode() && !status.finishArmed()) {
                return Component.translatable("tobacconistmod.display.finish_rearm");
            }
            if (status.uniform()) {
                return Component.translatable("tobacconistmod.display.batch_uniform", status.count(), status.predictedQuality());
            }
            if (status.ready()) {
                return status.finishMode()
                        ? Component.translatable("tobacconistmod.display.finish_ready", status.count())
                        : Component.translatable("tobacconistmod.display.batch_ready", status.count(), status.target());
            }
            return Component.translatable("tobacconistmod.display.batch_filling", status.count(), status.target());
        }
    }

    private static final class HomogenizationAverageSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            BasinBlockEntity basin = source(context, BasinBlockEntity.class);
            if (basin == null || basin.getLevel() == null) return EMPTY_LINE;

            CreateTobaccoHomogenization.HomogenizationStatus status =
                    CreateTobaccoHomogenization.getStatus(basin.getLevel(), basin.getBlockPos());
            if (!status.relevant()) return Component.translatable("tobacconistmod.ui.no_tobacco_batch");
            return Component.translatable(
                    "tobacconistmod.display.average_predicted",
                    String.format(java.util.Locale.ROOT, "%.1f", status.averageQuality()),
                    status.predictedQuality());
        }
    }

    private abstract static class VaultTobaccoSource extends TobacconistSingleLineSource {
        protected CreateTobaccoHomogenization.InventoryQualitySummary summary(DisplayLinkContext context) {
            ItemVaultBlockEntity vault = source(context, ItemVaultBlockEntity.class);
            if (vault == null || vault.getLevel() == null) {
                return CreateTobaccoHomogenization.InventoryQualitySummary.EMPTY;
            }
            IItemHandler handler = vault.getLevel().getCapability(
                    Capabilities.ItemHandler.BLOCK, vault.getBlockPos(), null);
            return CreateTobaccoHomogenization.summarizeInventory(handler);
        }
    }

    private static final class VaultTobaccoCountSource extends VaultTobaccoSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            CreateTobaccoHomogenization.InventoryQualitySummary summary = summary(context);
            return summary.present()
                    ? Component.translatable("tobacconistmod.display.tobacco_count", summary.count())
                    : Component.translatable("tobacconistmod.display.no_tobacco");
        }
    }

    private static final class VaultAverageQualitySource extends VaultTobaccoSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            CreateTobaccoHomogenization.InventoryQualitySummary summary = summary(context);
            if (!summary.present()) return Component.translatable("tobacconistmod.display.no_tobacco");
            if (!summary.compatible()) return Component.translatable("tobacconistmod.display.mixed_tobacco_lots");
            return Component.translatable(
                    "tobacconistmod.display.average_quality",
                    String.format(java.util.Locale.ROOT, "%.1f", summary.averageQuality()));
        }
    }

    private static final class HookahStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            HookahEntity hookah = source(context, HookahEntity.class);
            if (hookah == null) return EMPTY_LINE;

            if (hookah.getBlockState().hasProperty(BlockStateProperties.LIT)
                    && hookah.getBlockState().getValue(BlockStateProperties.LIT)) {
                return Component.translatable(hookah.isUsingDirtyWater()
                        ? "tobacconistmod.hookah.status.smoking_dirty_water"
                        : "tobacconistmod.hookah.status.smoking");
            }
            if (hookah.isUsingDirtyWater()) return Component.translatable("tobacconistmod.hookah.status.dirty_water");
            if (hookah.getItemHandler().getStackInSlot(1).isEmpty()) return Component.translatable("tobacconistmod.hookah.status.needs_shisha");
            if (hookah.getItemHandler().getStackInSlot(2).isEmpty()) return Component.translatable("tobacconistmod.hookah.status.needs_water");
            if (hookah.getItemHandler().getStackInSlot(0).isEmpty()) return Component.translatable("tobacconistmod.hookah.status.needs_fuel");
            return Component.translatable("tobacconistmod.ui.ready");
        }
    }

}