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

public class TobaccoBoxFillRecipeCategory implements IRecipeCategory<TobaccoBoxFillJeiRecipe> {

    public static final RecipeType<TobaccoBoxFillJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "tobacco_box_fill"), TobaccoBoxFillJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public TobaccoBoxFillRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(130, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.TOBACCO_BOX.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<TobaccoBoxFillJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Tobacco Box Filling");
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
    public void setRecipe(IRecipeLayoutBuilder builder, TobaccoBoxFillJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 22)
                .addItemStack(recipe.emptyBox());

        builder.addSlot(RecipeIngredientRole.INPUT, 32, 22)
                .addItemStacks(recipe.contents());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 22)
                .addItemStacks(recipe.outputs());
    }

    @Override
    public void draw(TobaccoBoxFillJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 21);
        slot.draw(guiGraphics, 31, 21);
        slot.draw(guiGraphics, 91, 21);

        guiGraphics.drawString(mc.font, "Box + tobacco product", 4, 4, 0x404040, false);
        guiGraphics.drawString(mc.font, "Only matching contents!", 4, 50, 0x808080, false);
    }
}