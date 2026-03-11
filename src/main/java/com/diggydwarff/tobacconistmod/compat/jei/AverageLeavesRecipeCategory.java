package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AverageLeavesRecipeCategory implements IRecipeCategory<AverageLeavesJeiRecipe> {

    public static final RecipeType<AverageLeavesJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "average_leaves"), AverageLeavesJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public AverageLeavesRecipeCategory(IGuiHelper guiHelper, IDrawable icon) {
        this.background = guiHelper.createBlankDrawable(116, 60);
        this.icon = icon;
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<AverageLeavesJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Average Tobacco Leaves");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AverageLeavesJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 20)
                .addItemStack(recipe.inputA());

        builder.addSlot(RecipeIngredientRole.INPUT, 32, 20)
                .addItemStack(recipe.inputB());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 20)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(AverageLeavesJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 19);
        slot.draw(guiGraphics, 31, 19);
        slot.draw(guiGraphics, 85, 19);

        guiGraphics.drawString(mc.font,
                Component.literal("Combine 2 matching leaves"),
                4, 4, 0x404040, false);

        guiGraphics.drawString(mc.font,
                Component.literal("into 1 averaged leaf"),
                4, 14, 0x404040, false);

        guiGraphics.drawString(mc.font,
                Component.literal("Example: 40 + 80 -> 60"),
                4, 46, 0x808080, false);
    }
}