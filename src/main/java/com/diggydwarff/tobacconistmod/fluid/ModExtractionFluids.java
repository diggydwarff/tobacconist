package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Real, non-placeable factory fluids for Aqua Vitae and concentrated flavoring essences. */
public final class ModExtractionFluids {
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TobacconistMod.MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, TobacconistMod.MODID);

    private static final Supplier<FluidType> AQUA_VITAE_TYPE =
            FLUID_TYPES.register("aqua_vitae", () -> new ExtractionFluidType());
    private static final Entry AQUA_VITAE = new Entry(AQUA_VITAE_TYPE);
    private static final Map<BottledMolassesFlavors, Entry> ESSENCES =
            new EnumMap<>(BottledMolassesFlavors.class);

    static {
        AQUA_VITAE.source = FLUIDS.register("aqua_vitae",
                () -> new BaseFlowingFluid.Source(AQUA_VITAE.properties()));
        AQUA_VITAE.flowing = FLUIDS.register("flowing_aqua_vitae",
                () -> new BaseFlowingFluid.Flowing(AQUA_VITAE.properties()));

        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            if (flavor.isPlain()) continue;
            registerEssence(flavor);
        }
    }

    private ModExtractionFluids() {}

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }

    public static Fluid aquaVitae() {
        return AQUA_VITAE.source.get();
    }

    public static Fluid essence(BottledMolassesFlavors flavor) {
        Entry entry = ESSENCES.get(flavor);
        return entry == null ? null : entry.source.get();
    }

    public static Optional<BottledMolassesFlavors> findEssenceFlavor(Fluid fluid) {
        if (fluid == null) return Optional.empty();
        for (Map.Entry<BottledMolassesFlavors, Entry> candidate : ESSENCES.entrySet()) {
            Entry entry = candidate.getValue();
            if (entry.source.get() == fluid || entry.flowing.get() == fluid) {
                return Optional.of(candidate.getKey());
            }
        }
        return Optional.empty();
    }

    public static boolean isAquaVitae(Fluid fluid) {
        return fluid != null && (AQUA_VITAE.source.get() == fluid || AQUA_VITAE.flowing.get() == fluid);
    }

    private static void registerEssence(BottledMolassesFlavors flavor) {
        Supplier<FluidType> type = FLUID_TYPES.register(flavor.getEssenceFluidName(),
                () -> new ExtractionFluidType(EssenceFlavorColors.fluidTint(flavor)));
        Entry entry = new Entry(type);
        entry.source = FLUIDS.register(flavor.getEssenceFluidName(),
                () -> new BaseFlowingFluid.Source(entry.properties()));
        entry.flowing = FLUIDS.register("flowing_" + flavor.getEssenceFluidName(),
                () -> new BaseFlowingFluid.Flowing(entry.properties()));
        ESSENCES.put(flavor, entry);
    }

    private static final class Entry {
        private final Supplier<? extends FluidType> type;
        private Supplier<? extends Fluid> source;
        private Supplier<? extends Fluid> flowing;

        private Entry(Supplier<? extends FluidType> type) {
            this.type = type;
        }

        private BaseFlowingFluid.Properties properties() {
            return new BaseFlowingFluid.Properties(type, source, flowing)
                    .tickRate(5)
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1);
        }
    }
}
