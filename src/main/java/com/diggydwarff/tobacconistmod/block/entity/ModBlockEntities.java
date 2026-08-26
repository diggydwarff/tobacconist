package com.diggydwarff.tobacconistmod.block.entity;

import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Supplier;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TobacconistMod.MODID);

    public static final Supplier<BlockEntityType<HookahEntity>> HOOKAH = BLOCK_ENTITIES.register("hookah", () ->
            BlockEntityType.Builder.of(
                    HookahEntity::new,
                    ModBlocks.HOOKAH.get(),
                    ModBlocks.TALL_HOOKAH.get(),
                    ModBlocks.ORNATE_COPPER_HOOKAH.get(),
                    ModBlocks.EXPOSED_COPPER_HOOKAH.get(),
                    ModBlocks.WEATHERED_COPPER_HOOKAH.get(),
                    ModBlocks.OXIDIZED_COPPER_HOOKAH.get(),
                    ModBlocks.WAXED_COPPER_HOOKAH.get(),
                    ModBlocks.WAXED_EXPOSED_COPPER_HOOKAH.get(),
                    ModBlocks.WAXED_WEATHERED_COPPER_HOOKAH.get(),
                    ModBlocks.WAXED_OXIDIZED_COPPER_HOOKAH.get(),
                    ModBlocks.ORNATE_GOLD_HOOKAH.get(),
                    ModBlocks.ORNATE_DIAMOND_HOOKAH.get(),
                    ModBlocks.ORNATE_IRON_HOOKAH.get(),
                    ModBlocks.ORNATE_AMETHYST_HOOKAH.get(),
                    ModBlocks.REDSTONE_HOOKAH.get(),
                    ModBlocks.LAPIS_HOOKAH.get(),
                    ModBlocks.OBSIDIAN_HOOKAH.get(),
                    ModBlocks.EMERALD_HOOKAH.get(),
                    ModBlocks.NETHERITE_HOOKAH.get()
            ).build(null));

    public static final Supplier<BlockEntityType<TobaccoDryingRackBlockEntity>> TOBACCO_DRYING_RACK =
            BLOCK_ENTITIES.register("tobacco_drying_rack", () ->
                    BlockEntityType.Builder.of(
                            TobaccoDryingRackBlockEntity::new,
                            ModBlocks.TOBACCO_DRYING_RACK.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<IndustrialDryingRackBlockEntity>> INDUSTRIAL_DRYING_RACK =
            BLOCK_ENTITIES.register("industrial_drying_rack", () ->
                    BlockEntityType.Builder.of(
                            IndustrialDryingRackBlockEntity::new,
                            ModBlocks.INDUSTRIAL_DRYING_RACK.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<TobaccoCrateBlockEntity>> TOBACCO_CRATE =
            BLOCK_ENTITIES.register("tobacco_crate", () ->
                    BlockEntityType.Builder.of(
                            TobaccoCrateBlockEntity::new,
                            ModBlocks.WILD_TOBACCO_CRATE.get(),
                            ModBlocks.VIRGINIA_TOBACCO_CRATE.get(),
                            ModBlocks.BURLEY_TOBACCO_CRATE.get(),
                            ModBlocks.ORIENTAL_TOBACCO_CRATE.get(),
                            ModBlocks.DOKHA_TOBACCO_CRATE.get(),
                            ModBlocks.SHADE_TOBACCO_CRATE.get(),
                            ModBlocks.RAW_WILD_TOBACCO_CRATE.get(),
                            ModBlocks.RAW_VIRGINIA_TOBACCO_CRATE.get(),
                            ModBlocks.RAW_BURLEY_TOBACCO_CRATE.get(),
                            ModBlocks.RAW_ORIENTAL_TOBACCO_CRATE.get(),
                            ModBlocks.RAW_DOKHA_TOBACCO_CRATE.get(),
                            ModBlocks.RAW_SHADE_TOBACCO_CRATE.get(),
                            ModBlocks.BLENDED_TOBACCO_CRATE.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<HangingTobaccoBlockEntity>> HANGING_TOBACCO =
            BLOCK_ENTITIES.register("hanging_tobacco", () ->
                    BlockEntityType.Builder.of(
                            HangingTobaccoBlockEntity::new,
                            ModBlocks.HANGING_TOBACCO_LEAVES.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<FlueFireboxBlockEntity>> FLUE_FIREBOX =
            BLOCK_ENTITIES.register("flue_firebox",
                    () -> BlockEntityType.Builder.of(
                            FlueFireboxBlockEntity::new,
                            ModBlocks.FLUE_FIREBOX.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<TobaccoBarrelBlockEntity>> TOBACCO_BARREL =
            BLOCK_ENTITIES.register("tobacco_barrel",
                    () -> BlockEntityType.Builder.of(
                            TobaccoBarrelBlockEntity::new,
                            ModBlocks.TOBACCO_BARREL.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<ProductionMonitorBlockEntity>> PRODUCTION_MONITOR =
            BLOCK_ENTITIES.register("production_monitor",
                    () -> BlockEntityType.Builder.of(
                            ProductionMonitorBlockEntity::new,
                            ModBlocks.PRODUCTION_MONITOR.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
