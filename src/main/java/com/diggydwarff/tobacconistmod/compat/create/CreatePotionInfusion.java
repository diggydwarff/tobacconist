package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.PotionInfusion;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** Uses the actual potion components in the Spout, including custom potion contents. */
public final class CreatePotionInfusion {
    private CreatePotionInfusion() {}
    public static boolean matches(ItemStack stack, FluidStack fluid) {
        return PotionInfusion.canTreat(stack) && fluid.getFluid() instanceof PotionFluid
                && !PotionInfusion.prepare(fluid.get(DataComponents.POTION_CONTENTS)).isEmpty();
    }
    public static ItemStack fill(int required, ItemStack stack, FluidStack fluid) {
        if (required != PotionInfusion.FLUID_DOSE || fluid.getAmount() < required || !matches(stack, fluid)) return ItemStack.EMPTY;
        ItemStack result = PotionInfusion.treat(stack, fluid.get(DataComponents.POTION_CONTENTS));
        if (!result.isEmpty()) {
            stack.shrink(1);
            fluid.shrink(required);
        }
        return result;
    }
}
