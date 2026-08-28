package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
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
import net.minecraft.world.item.ItemStack;

public final class HomogenizationRecipeCategory implements IRecipeCategory<HomogenizationJeiRecipe> {
    public static final RecipeType<HomogenizationJeiRecipe> TYPE = new RecipeType<>(
            new ResourceLocation(TobacconistMod.MODID, "tobacco_homogenization"),
            HomogenizationJeiRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public HomogenizationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(164, 70);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.VIRGINIA_TOBACCO_LEAF_DRY.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<HomogenizationJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tobacconistmod.jei.tobacco_homogenization");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HomogenizationJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 14, 27)
                .addItemStack(recipe.lowQuality());
        builder.addSlot(RecipeIngredientRole.INPUT, 38, 27)
                .addItemStack(recipe.highQuality());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 27)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(HomogenizationJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        slot.draw(guiGraphics, 13, 26);
        slot.draw(guiGraphics, 37, 26);
        slot.draw(guiGraphics, 125, 26);
        guiGraphics.drawString(mc.font, "+", 31, 32, 0x404040, false);
        guiGraphics.drawString(mc.font, ">", 88, 32, 0x404040, false);
        guiGraphics.drawString(mc.font,
                Component.translatable("tobacconistmod.jei.homogenization.line1"),
                4, 4, 0x404040, false);
        guiGraphics.drawString(mc.font,
                Component.translatable("tobacconistmod.jei.homogenization.line2"),
                4, 56, 0x808080, false);
    }
}
