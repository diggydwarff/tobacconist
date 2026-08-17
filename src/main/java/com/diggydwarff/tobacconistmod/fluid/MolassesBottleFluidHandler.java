package com.diggydwarff.tobacconistmod.fluid;

import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Exposes the existing four-use molasses bottle as a standard 1000 mB NeoForge fluid container.
 * Each durability point corresponds to one 250 mB shisha dose.
 */
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
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return 0;
        }
        int usesLeft = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
        return Math.min(CAPACITY, usesLeft * DOSE);
    }

    public static boolean hasDose(ItemStack stack) {
        return amountIn(stack) >= DOSE;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0) {
            return FluidStack.EMPTY;
        }
        int amount = amountIn(container);
        return amount <= 0 ? FluidStack.EMPTY : new FluidStack(ModMolassesFluids.source(flavor), amount);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? CAPACITY : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && !stack.isEmpty() && stack.getFluid() == ModMolassesFluids.source(flavor);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidValid(0, resource)) {
            return 0;
        }

        int stored = amountIn(container);
        int room = CAPACITY - stored;
        int fillable = Math.min(room, resource.getAmount());
        fillable -= fillable % DOSE;
        if (fillable <= 0) {
            return 0;
        }

        if (action.execute()) {
            setAmount(stored + fillable);
        }
        return fillable;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || resource.getFluid() != ModMolassesFluids.source(flavor)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        int stored = amountIn(container);
        int drainable = Math.min(stored, maxDrain);
        drainable -= drainable % DOSE;
        if (drainable <= 0) {
            return FluidStack.EMPTY;
        }

        if (action.execute()) {
            setAmount(stored - drainable);
        }
        return new FluidStack(ModMolassesFluids.source(flavor), drainable);
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    private void setAmount(int amount) {
        int clamped = Math.max(0, Math.min(CAPACITY, amount));
        if (clamped == 0) {
            container = new ItemStack(Items.GLASS_BOTTLE);
            return;
        }

        int usesLeft = clamped / DOSE;
        container.setDamageValue(Math.max(0, container.getMaxDamage() - usesLeft));
    }
}
