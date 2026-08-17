package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Lets a normal vanilla glass bottle be filled with any Tobacconist molasses fluid.
 * Once fluid is inserted, the returned container becomes that flavor's normal molasses bottle.
 */
public final class GlassBottleMolassesFluidHandler implements IFluidHandlerItem {
    private ItemStack container;
    private BottledMolassesFlavors flavor;
    private int amount;

    public GlassBottleMolassesFluidHandler(ItemStack container) {
        this.container = container.copy();
        this.container.setCount(1);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || flavor == null || amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(ModMolassesFluids.source(flavor), amount);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? MolassesBottleFluidHandler.CAPACITY : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank != 0 || stack.isEmpty()) {
            return false;
        }
        BottledMolassesFlavors candidate = ModMolassesFluids.findFlavor(stack.getFluid()).orElse(null);
        return candidate != null && (flavor == null || flavor == candidate);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        BottledMolassesFlavors candidate = ModMolassesFluids.findFlavor(resource.getFluid()).orElse(null);
        if (candidate == null || (flavor != null && flavor != candidate)) {
            return 0;
        }

        int room = MolassesBottleFluidHandler.CAPACITY - amount;
        int fillable = Math.min(room, resource.getAmount());
        fillable -= fillable % MolassesBottleFluidHandler.DOSE;
        if (fillable <= 0) {
            return 0;
        }

        if (action.execute()) {
            flavor = candidate;
            amount += fillable;

            ItemStack filled = new ItemStack(flavor.getItem());
            int usesLeft = amount / MolassesBottleFluidHandler.DOSE;
            filled.setDamageValue(Math.max(0, filled.getMaxDamage() - usesLeft));
            container = filled;
        }
        return fillable;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }
}
