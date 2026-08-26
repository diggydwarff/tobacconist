package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/** Single-use 1000 mB fluid container backing every Flavoring Essence bottle. */
public final class EssenceBottleFluidHandler implements IFluidHandlerItem {
    public static final int CAPACITY = 1000;
    public static final int DOSE = 1000;

    private ItemStack container;
    private final BottledMolassesFlavors flavor;

    public EssenceBottleFluidHandler(ItemStack container, BottledMolassesFlavors flavor) {
        this.container = container;
        this.flavor = flavor;
    }

    public static int amountIn(ItemStack stack) {
        return stack.isEmpty() ? 0 : CAPACITY;
    }

    public static boolean hasDose(ItemStack stack) { return amountIn(stack) >= DOSE; }
    public static boolean isFull(ItemStack stack) { return amountIn(stack) == CAPACITY; }

    @Override public int getTanks() { return 1; }

    @Override
    public FluidStack getFluidInTank(int tank) {
        int amount = tank == 0 ? amountIn(container) : 0;
        return amount <= 0 ? FluidStack.EMPTY : new FluidStack(ModExtractionFluids.essence(flavor), amount);
    }

    @Override public int getTankCapacity(int tank) { return tank == 0 ? CAPACITY : 0; }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && !stack.isEmpty() && stack.getFluid() == ModExtractionFluids.essence(flavor);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidValid(0, resource)) return 0;
        int stored = amountIn(container);
        int fillable = Math.min(CAPACITY - stored, resource.getAmount());
        fillable -= fillable % DOSE;
        if (fillable <= 0) return 0;
        if (action.execute()) setAmount(stored + fillable);
        return fillable;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || resource.getFluid() != ModExtractionFluids.essence(flavor)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        int stored = amountIn(container);
        int drainable = Math.min(stored, maxDrain);
        drainable -= drainable % DOSE;
        if (drainable <= 0) return FluidStack.EMPTY;
        if (action.execute()) setAmount(stored - drainable);
        return new FluidStack(ModExtractionFluids.essence(flavor), drainable);
    }

    @Override public ItemStack getContainer() { return container; }

    private void setAmount(int amount) {
        int clamped = Math.max(0, Math.min(CAPACITY, amount));
        if (clamped == 0) {
            container = new ItemStack(Items.GLASS_BOTTLE);
            return;
        }
        if (container.isEmpty() || container.getItem() != flavor.getEssenceItem()) {
            container = flavor.getEssenceStack();
        }
    }
}
