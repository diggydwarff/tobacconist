package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/** A molasses bottle is one full 1000 mB batch; flavored bottles are consumed whole by Shisha. */
public final class MolassesBottleFluidHandler implements IFluidHandlerItem {
    public static final int CAPACITY = 1000;
    public static final int DOSE = 250;

    private ItemStack container;
    private final BottledMolassesFlavors flavor;

    public MolassesBottleFluidHandler(ItemStack container, BottledMolassesFlavors flavor) {
        this.container = container;
        this.flavor = flavor;
    }

    public static int amountIn(ItemStack stack) {
        return stack.isEmpty() || BottledMolassesFlavors.fromItem(stack.getItem()) == null ? 0 : CAPACITY;
    }

    public static boolean isFullBottle(ItemStack stack) {
        return amountIn(stack) == CAPACITY;
    }

    @Override public int getTanks() { return 1; }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 && !container.isEmpty()
                ? new FluidStack(ModMolassesFluids.source(flavor), CAPACITY)
                : FluidStack.EMPTY;
    }

    @Override public int getTankCapacity(int tank) { return tank == 0 ? CAPACITY : 0; }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && !stack.isEmpty() && stack.getFluid() == ModMolassesFluids.source(flavor);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        // Registered molasses items are already full. Empty vanilla bottles use the generic handler.
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || resource.getFluid() != ModMolassesFluids.source(flavor)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (container.isEmpty() || maxDrain < CAPACITY) return FluidStack.EMPTY;
        if (action.execute()) container = new ItemStack(Items.GLASS_BOTTLE);
        return new FluidStack(ModMolassesFluids.source(flavor), CAPACITY);
    }

    @Override public ItemStack getContainer() { return container; }
}
