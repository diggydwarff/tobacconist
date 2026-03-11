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

public class TobaccoBoxLabelRecipeCategory implements IRecipeCategory<TobaccoBoxLabelJeiRecipe> {

    public static final RecipeType<TobaccoBoxLabelJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "tobacco_box_label"), TobaccoBoxLabelJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public TobaccoBoxLabelRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 58);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.TOBACCO_LABEL.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<TobaccoBoxLabelJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Tobacco Box Labeling");
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
    public void setRecipe(IRecipeLayoutBuilder builder, TobaccoBoxLabelJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 20)
                .addItemStack(recipe.box());

        builder.addSlot(RecipeIngredientRole.INPUT, 32, 20)
                .addItemStack(recipe.label());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 20)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(TobaccoBoxLabelJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 19);
        slot.draw(guiGraphics, 31, 19);
        slot.draw(guiGraphics, 85, 19);

        guiGraphics.drawString(mc.font, "Filled box + named label", 4, 4, 0x404040, false);
    }
}