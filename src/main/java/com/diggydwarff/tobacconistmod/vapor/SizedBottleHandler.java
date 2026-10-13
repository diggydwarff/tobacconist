package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** Small/large glass works with partial transfers, Spouts and Item Drains. */
public final class SizedBottleHandler implements IFluidHandlerItem {
    private ItemStack container;

    public SizedBottleHandler(ItemStack stack) { container = stack.copyWithCount(1); }
    @Override public int getTanks() { return 1; }
    @Override public int getTankCapacity(int tank) { return tank == 0 ? VaporData.bottleCapacity(container) : 0; }
    @Override public ItemStack getContainer() { return container; }
    @Override public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? SizedBottleItem.fluid(container) : FluidStack.EMPTY;
    }
    @Override public boolean isFluidValid(int tank, FluidStack fluid) {
        FluidStack current = getFluidInTank(0);
        return tank == 0 && BottledFluid.supported(fluid)
                && (current.isEmpty() || FluidStack.isSameFluidSameComponents(current, fluid));
    }
    @Override public int fill(FluidStack resource, FluidAction action) {
        if (!isFluidValid(0, resource)) return 0;
        int current = getFluidInTank(0).getAmount();
        int amount = Math.min(getTankCapacity(0) - current, resource.getAmount());
        if (amount <= 0) return 0;
        if (action.execute()) {
            if (SizedBottleItem.isEmptyGlass(container)) container = SizedBottleItem.filled(container, resource, current + amount);
            else container.set(VaporContent.BOTTLED_FLUID.get(), BottledFluid.of(resource, current + amount));
        }
        return amount;
    }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.isSameFluidSameComponents(getFluidInTank(0), resource) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
    }
    @Override public FluidStack drain(int maximum, FluidAction action) {
        FluidStack current = getFluidInTank(0);
        int amount = Math.min(Math.max(0, maximum), current.getAmount());
        if (amount == 0) return FluidStack.EMPTY;
        if (action.execute()) {
            int remaining = current.getAmount() - amount;
            if (remaining == 0) container = VaporData.emptyGlass(container);
            else container.set(VaporContent.BOTTLED_FLUID.get(), BottledFluid.of(current, remaining));
        }
        return current.copyWithAmount(amount);
    }
}
