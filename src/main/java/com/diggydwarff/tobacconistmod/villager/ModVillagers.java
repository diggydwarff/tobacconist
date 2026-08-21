package com.diggydwarff.tobacconistmod.villager;

import java.util.function.Supplier;
import com.google.common.collect.ImmutableSet;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, TobacconistMod.MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, TobacconistMod.MODID);

    public static final Supplier<PoiType> HOOKAH_POI =
            POI_TYPES.register("hookah_poi", () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.HOOKAH.get().getStateDefinition().getPossibleStates()),
                    1, 1
            ));

    public static final Supplier<VillagerProfession> TOBACCONIST_MASTER =
            VILLAGER_PROFESSIONS.register("tobacconist_master", () -> new VillagerProfession(
                    "tobacconist_master",
                    poiHolder -> poiHolder.value() == HOOKAH_POI.get(),
                    poiHolder -> poiHolder.value() == HOOKAH_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_ARMORER
            ));


    public static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        VILLAGER_PROFESSIONS.register(bus);
    }
}