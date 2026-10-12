package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** Partial draining is supported; the original glass is returned exactly when empty. */
public final class VaporBottleHandler implements IFluidHandlerItem {
    private ItemStack container;
    public VaporBottleHandler(ItemStack stack) { container = stack; }
    @Override public int getTanks() { return 1; }
    @Override public int getTankCapacity(int tank) { return tank == 0 ? VaporData.BOTTLE : 0; }
    @Override public boolean isFluidValid(int tank, FluidStack fluid) { return tank == 0 && FluidStack.isSameFluidSameComponents(getFluidInTank(0), fluid); }
    @Override public ItemStack getContainer() { return container; }

    @Override public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || container.isEmpty() || container.is(Items.GLASS_BOTTLE)) return FluidStack.EMPTY;
        int volume = VaporData.volume(container);
        if (volume == 0) return FluidStack.EMPTY;
        if (container.is(VaporContent.VEGETABLE_BASE.get())) return new FluidStack(VaporContent.VEGETABLE_FLUID.get(), volume);
        if (container.is(VaporContent.CLEAR_BASE.get())) return new FluidStack(VaporContent.CLEAR_FLUID.get(), volume);
        if (container.is(VaporContent.NICOTINE_EXTRACT.get())) return new FluidStack(VaporContent.NICOTINE_FLUID.get(), volume);
        if (container.is(VaporContent.CRUDE_EXTRACT.get())) return new FluidStack(VaporContent.CRUDE_FLUID.get(), volume);
        LiquidBlend blend = VaporData.blend(container);
        return blend == null ? FluidStack.EMPTY : VaporData.fluid(blend, volume);
    }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (!isFluidValid(0, resource)) return 0;
        int amount = Math.min(VaporData.BOTTLE - VaporData.volume(container), resource.getAmount());
        if (action.execute() && amount > 0) container.set(VaporContent.VOLUME.get(), VaporData.volume(container) + amount);
        return amount;
    }

    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.isSameFluidSameComponents(getFluidInTank(0), resource) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
    }

    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack fluid = getFluidInTank(0);
        int amount = Math.min(Math.max(0, maxDrain), fluid.getAmount());
        if (amount == 0) return FluidStack.EMPTY;
        FluidStack result = fluid.copyWithAmount(amount);
        if (action.execute()) {
            int remaining = fluid.getAmount() - amount;
            if (remaining == 0) container = new ItemStack(Items.GLASS_BOTTLE);
            else container.set(VaporContent.VOLUME.get(), remaining);
        }
        return result;
    }

    public static boolean supported(FluidStack stack) {
        return !stack.isEmpty() && (stack.getFluid() == VaporContent.VEGETABLE_FLUID.get()
                || stack.getFluid() == VaporContent.CLEAR_FLUID.get() || stack.getFluid() == VaporContent.CRUDE_FLUID.get()
                || stack.getFluid() == VaporContent.NICOTINE_FLUID.get()
                || stack.getFluid() == VaporContent.LIQUID_FLUID.get() && VaporData.blend(stack) != null);
    }

    public static ItemStack filled(FluidStack fluid) {
        if (!supported(fluid)) return ItemStack.EMPTY;
        if (fluid.getFluid() == VaporContent.LIQUID_FLUID.get()) return VaporData.bottle(VaporData.blend(fluid), VaporData.BOTTLE);
        var item = fluid.getFluid() == VaporContent.VEGETABLE_FLUID.get() ? VaporContent.VEGETABLE_BASE
                : fluid.getFluid() == VaporContent.CLEAR_FLUID.get() ? VaporContent.CLEAR_BASE
                : fluid.getFluid() == VaporContent.CRUDE_FLUID.get() ? VaporContent.CRUDE_EXTRACT : VaporContent.NICOTINE_EXTRACT;
        return new ItemStack(item.get());
    }
}
