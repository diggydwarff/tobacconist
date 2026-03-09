package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TobacconistMod.MODID);

    public static final RegistryObject<BlockEntityType<HookahEntity>> HOOKAH = BLOCK_ENTITIES.register("hookah", () ->
            BlockEntityType.Builder.of(
                    HookahEntity::new,
                    ModBlocks.HOOKAH.get(),
                    ModBlocks.ORNATE_COPPER_HOOKAH.get(),
                    ModBlocks.ORNATE_GOLD_HOOKAH.get(),
                    ModBlocks.ORNATE_DIAMOND_HOOKAH.get(),
                    ModBlocks.ORNATE_IRON_HOOKAH.get(),
                    ModBlocks.ORNATE_AMETHYST_HOOKAH.get()
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}