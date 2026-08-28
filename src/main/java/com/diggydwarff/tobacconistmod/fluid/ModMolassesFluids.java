package com.diggydwarff.tobacconistmod.fluid;

import net.minecraftforge.registries.ForgeRegistries;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Registers one non-placeable molasses fluid per Tobacconist flavor.
 *
 * <p>No fluid block or bucket is registered. Bottles represent full 250 mB batches, while
 * Create/Forge tanks and pipes handle the registered fluids directly.</p>
 */
public final class ModMolassesFluids {
    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TobacconistMod.MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, TobacconistMod.MODID);

    private static final Map<BottledMolassesFlavors, Entry> ENTRIES =
            new EnumMap<>(BottledMolassesFlavors.class);

    static {
        for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
            registerFlavor(flavor);
        }
    }

    private ModMolassesFluids() {}

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }

    public static Fluid source(BottledMolassesFlavors flavor) {
        Entry entry = ENTRIES.get(flavor);
        return entry == null ? null : entry.source.get();
    }

    public static Fluid flowing(BottledMolassesFlavors flavor) {
        Entry entry = ENTRIES.get(flavor);
        return entry == null ? null : entry.flowing.get();
    }

    public static Optional<BottledMolassesFlavors> findFlavor(Fluid fluid) {
        if (fluid == null) {
            return Optional.empty();
        }
        for (Map.Entry<BottledMolassesFlavors, Entry> candidate : ENTRIES.entrySet()) {
            Entry entry = candidate.getValue();
            if (entry.source.get() == fluid || entry.flowing.get() == fluid) {
                return Optional.of(candidate.getKey());
            }
        }
        return Optional.empty();
    }

    private static void registerFlavor(BottledMolassesFlavors flavor) {
        Supplier<FluidType> type = FLUID_TYPES.register(flavor.getFluidName(), MolassesFluidType::new);
        Entry entry = new Entry(type);

        entry.source = FLUIDS.register(flavor.getFluidName(),
                () -> new ForgeFlowingFluid.Source(entry.properties()));
        entry.flowing = FLUIDS.register("flowing_" + flavor.getFluidName(),
                () -> new ForgeFlowingFluid.Flowing(entry.properties()));

        ENTRIES.put(flavor, entry);
    }

    private static final class Entry {
        private final Supplier<? extends FluidType> type;
        private Supplier<? extends Fluid> source;
        private Supplier<? extends Fluid> flowing;

        private Entry(Supplier<? extends FluidType> type) {
            this.type = type;
        }

        private ForgeFlowingFluid.Properties properties() {
            return new ForgeFlowingFluid.Properties(type, source, flowing)
                    .tickRate(20)
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(2);
        }
    }
}
