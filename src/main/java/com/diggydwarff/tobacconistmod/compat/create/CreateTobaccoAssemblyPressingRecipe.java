package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.TobaccoAssemblyHelper;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraft.world.level.Level;

/** Final Mechanical Press stage for metadata-preserving cigarette/cigar assembly. */
public final class CreateTobaccoAssemblyPressingRecipe extends PressingRecipe {
    public CreateTobaccoAssemblyPressingRecipe(ProcessingRecipeParams params) {
        super(params);
    }

    @Override
    public boolean matches(RecipeWrapper input, Level level) {
        if (!super.matches(input, level)) return false;

        ItemStack result = TobaccoAssemblyHelper.finish(input.getItem(0));
        if (result.isEmpty()) return false;

        ItemStack template = result.copy();
        template.setCount(1);
        enforceNextResult(template::copy);
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CreatePressCompat.TOBACCO_ASSEMBLY_PRESSING.get();
    }
}
