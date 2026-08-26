package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/** Lets a vanilla glass bottle receive molasses, Aqua Vitae, or a Flavoring Essence from fluid automation. */
public final class GlassBottleMolassesFluidHandler implements IFluidHandlerItem {
    private ItemStack container;
    private int amount;
    private Kind kind = Kind.NONE;
    private BottledMolassesFlavors flavor;

    private enum Kind { NONE, MOLASSES, AQUA_VITAE, ESSENCE }

    public GlassBottleMolassesFluidHandler(ItemStack container) {
        this.container = container.copy();
        this.container.setCount(1);
    }

    @Override public int getTanks() { return 1; }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || amount <= 0) return FluidStack.EMPTY;
        return switch (kind) {
            case MOLASSES -> new FluidStack(ModMolassesFluids.source(flavor), amount);
            case AQUA_VITAE -> new FluidStack(ModExtractionFluids.aquaVitae(), amount);
            case ESSENCE -> new FluidStack(ModExtractionFluids.essence(flavor), amount);
            default -> FluidStack.EMPTY;
        };
    }

    @Override public int getTankCapacity(int tank) { return tank == 0 ? 1000 : 0; }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank != 0 || stack.isEmpty()) return false;
        Kind candidateKind = kindOf(stack);
        BottledMolassesFlavors candidateFlavor = flavorOf(stack, candidateKind);
        if (candidateKind == Kind.NONE) return false;
        return kind == Kind.NONE || (kind == candidateKind && flavor == candidateFlavor);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        Kind candidateKind = kindOf(resource);
        BottledMolassesFlavors candidateFlavor = flavorOf(resource, candidateKind);
        if (candidateKind == Kind.NONE || (kind != Kind.NONE && (kind != candidateKind || flavor != candidateFlavor))) {
            return 0;
        }

        int increment = 1000;
        int fillable = Math.min(1000 - amount, resource.getAmount());
        fillable -= fillable % increment;
        if (fillable <= 0) return 0;

        if (action.execute()) {
            kind = candidateKind;
            flavor = candidateFlavor;
            amount += fillable;
            container = makeContainer();
        }
        return fillable;
    }

    @Override public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) { return FluidStack.EMPTY; }
    @Override public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) { return FluidStack.EMPTY; }
    @Override public ItemStack getContainer() { return container; }

    private ItemStack makeContainer() {
        if (kind == Kind.MOLASSES && flavor != null) return flavor.getStack();
        if (kind == Kind.AQUA_VITAE) return new ItemStack(ModItems.BOTTLED_AQUA_VITAE.get());
        if (kind == Kind.ESSENCE && flavor != null) return flavor.getEssenceStack();
        return container;
    }

    private static Kind kindOf(FluidStack stack) {
        if (ModMolassesFluids.findFlavor(stack.getFluid()).isPresent()) return Kind.MOLASSES;
        if (ModExtractionFluids.isAquaVitae(stack.getFluid())) return Kind.AQUA_VITAE;
        if (ModExtractionFluids.findEssenceFlavor(stack.getFluid()).isPresent()) return Kind.ESSENCE;
        return Kind.NONE;
    }

    private static BottledMolassesFlavors flavorOf(FluidStack stack, Kind kind) {
        return switch (kind) {
            case MOLASSES -> ModMolassesFluids.findFlavor(stack.getFluid()).orElse(null);
            case ESSENCE -> ModExtractionFluids.findEssenceFlavor(stack.getFluid()).orElse(null);
            default -> null;
        };
    }
}
