package com.diggydwarff.tobacconistmod.backport;
import net.minecraftforge.fluids.FluidStack;
public final class FluidStacks {
    private FluidStacks() {}
    public static FluidStack withAmount(FluidStack value,int amount) {
        FluidStack copy=value.copy();copy.setAmount(Math.max(0,amount));return copy;
    }
    public static boolean same(FluidStack a,FluidStack b) { return a.isFluidEqual(b); }
}
