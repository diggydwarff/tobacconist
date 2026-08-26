package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/** One Bottle of Aqua Vitae equals one 1000 mB factory-fluid container. */
public final class AquaVitaeBottleFluidHandler implements IFluidHandlerItem {
    public static final int CAPACITY = 1000;
    private ItemStack container;

    public AquaVitaeBottleFluidHandler(ItemStack container) { this.container = container; }

    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) {
        return tank == 0 && container.getItem() == ModItems.BOTTLED_AQUA_VITAE.get()
                ? new FluidStack(ModExtractionFluids.aquaVitae(), CAPACITY) : FluidStack.EMPTY;
    }
    @Override public int getTankCapacity(int tank) { return tank == 0 ? CAPACITY : 0; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && !stack.isEmpty() && ModExtractionFluids.isAquaVitae(stack.getFluid());
    }
    @Override public int fill(FluidStack resource, IFluidHandler.FluidAction action) { return 0; }
    @Override public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidValid(0, resource)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }
    @Override public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (container.getItem() != ModItems.BOTTLED_AQUA_VITAE.get() || maxDrain < CAPACITY) return FluidStack.EMPTY;
        if (action.execute()) container = new ItemStack(Items.GLASS_BOTTLE);
        return new FluidStack(ModExtractionFluids.aquaVitae(), CAPACITY);
    }
    @Override public ItemStack getContainer() { return container; }
}
