package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import com.diggydwarff.tobacconistmod.fluid.ModMolassesFluids;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import java.util.Optional;

/** Immutable bottle contents: capacity belongs to the glass item, not its saved fluid. */
public record BottledFluid(ResourceLocation fluid, int amount, Optional<LiquidBlend> blend) {
    public static final Codec<BottledFluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(BottledFluid::fluid),
            Codec.intRange(1, 1000).fieldOf("amount").forGetter(BottledFluid::amount),
            VaporData.CODEC.optionalFieldOf("blend").forGetter(BottledFluid::blend)
    ).apply(instance, BottledFluid::new));

    public static BottledFluid of(FluidStack stack, int amount) {
        return new BottledFluid(BuiltInRegistries.FLUID.getKey(stack.getFluid()), amount,
                Optional.ofNullable(com.diggydwarff.tobacconistmod.backport.ComponentData.get(stack, VaporContent.BLEND.get())));
    }

    public BottledFluid withAmount(int amount) { return new BottledFluid(fluid, amount, blend); }

    public FluidStack asFluid(int capacity) {
        var type = BuiltInRegistries.FLUID.get(fluid);
        if (type == Fluids.EMPTY || amount <= 0) return FluidStack.EMPTY;
        FluidStack result = new FluidStack(type, Math.min(amount, capacity));
        blend.ifPresent(value -> com.diggydwarff.tobacconistmod.backport.ComponentData.set(result, VaporContent.BLEND.get(), value));
        return result;
    }

    public static boolean supported(FluidStack stack) {
        return VaporBottleHandler.supported(stack) || !stack.isEmpty() && (stack.getFluid() == Fluids.WATER
                || ModExtractionFluids.isAquaVitae(stack.getFluid())
                || ModExtractionFluids.findEssenceFlavor(stack.getFluid()).isPresent()
                || ModMolassesFluids.findFlavor(stack.getFluid()).isPresent());
    }
}
