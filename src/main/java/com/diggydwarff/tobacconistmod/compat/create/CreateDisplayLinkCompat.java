package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.FlueFireboxBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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

            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_PROGRESS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_HUMIDITY.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.TOBACCO_BARREL.get(), BARREL_AGE.get());

            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FLUE_FIREBOX.get(), FLUE_FIREBOX_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FLUE_FIREBOX.get(), FLUE_FIREBOX_FUEL.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.HOOKAH.get(), HOOKAH_STATUS.get());
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
            return rack == null ? EMPTY_LINE : Component.literal(rack.getRackStatusText());
        }
    }

    private static final class DryingRackProgressSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoDryingRackBlockEntity rack = source(context, TobaccoDryingRackBlockEntity.class);
            return rack == null ? EMPTY_LINE : Component.literal(rack.getDryProgressPercent() + "%");
        }
    }

    private static final class DryingRackLeafCountSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoDryingRackBlockEntity rack = source(context, TobaccoDryingRackBlockEntity.class);
            return rack == null ? EMPTY_LINE : Component.literal(Integer.toString(rack.getLeafCount()));
        }
    }

    private static final class BarrelStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            if (barrel == null) return EMPTY_LINE;
            if (barrel.getStoredTobacco().isEmpty()) return Component.literal("Empty");

            String mode = barrel.getModeNameForInspection();
            int progress = barrel.getProcessProgressPercent();
            if (progress > 0) {
                return Component.literal(mode + " - " + progress + "%");
            }
            return Component.literal(mode);
        }
    }

    private static final class BarrelProgressSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            return barrel == null ? EMPTY_LINE : Component.literal(barrel.getProcessProgressPercent() + "%");
        }
    }

    private static final class BarrelHumiditySource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            return barrel == null ? EMPTY_LINE : Component.literal(barrel.getBarrelHumidity() + "%");
        }
    }

    private static final class BarrelAgeSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            TobaccoBarrelBlockEntity barrel = source(context, TobaccoBarrelBlockEntity.class);
            if (barrel == null) return EMPTY_LINE;

            ItemStack stored = barrel.getStoredTobacco();
            if (stored.isEmpty()) return Component.literal("0d");

            int days = TobaccoBarrelBlockEntity.getAgedDays(stored);
            if (days < 365) return Component.literal(days + "d");
            return Component.literal((days / 365) + "y " + (days % 365) + "d");
        }
    }

    private static final class FlueFireboxStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            FlueFireboxBlockEntity firebox = source(context, FlueFireboxBlockEntity.class);
            return firebox == null ? EMPTY_LINE : Component.literal(firebox.isLit() ? "Lit" : "Idle");
        }
    }

    private static final class FlueFireboxFuelSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            FlueFireboxBlockEntity firebox = source(context, FlueFireboxBlockEntity.class);
            if (firebox == null) return EMPTY_LINE;
            int total = firebox.getBurnTimeTotal();
            int percent = total <= 0 ? 0 : Math.min(100, firebox.getBurnTime() * 100 / total);
            return Component.literal(percent + "%");
        }
    }

    private static final class HookahStatusSource extends TobacconistSingleLineSource {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            HookahEntity hookah = source(context, HookahEntity.class);
            if (hookah == null) return EMPTY_LINE;

            if (hookah.getBlockState().hasProperty(BlockStateProperties.LIT)
                    && hookah.getBlockState().getValue(BlockStateProperties.LIT)) {
                return Component.literal("Smoking");
            }
            if (hookah.getItemHandler().getStackInSlot(1).isEmpty()) return Component.literal("Needs Shisha");
            if (hookah.getItemHandler().getStackInSlot(2).isEmpty()) return Component.literal("Needs Water");
            if (hookah.getItemHandler().getStackInSlot(0).isEmpty()) return Component.literal("Needs Fuel");
            return Component.literal("Ready");
        }
    }

}
