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

public class CigaretteRecipeCategory implements IRecipeCategory<CigaretteJeiRecipe> {

    public static final RecipeType<CigaretteJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "cigarette_rolling"), CigaretteJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public CigaretteRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(116, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.CIGARETTE.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<CigaretteJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Cigarette Rolling");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CigaretteJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 18)
                .addItemStack(recipe.tobacco());

        builder.addSlot(RecipeIngredientRole.INPUT, 32, 18)
                .addItemStack(recipe.paper());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 18)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(CigaretteJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 17);
        slot.draw(guiGraphics, 31, 17);
        slot.draw(guiGraphics, 85, 17);

        guiGraphics.drawString(mc.font,
                Component.literal("Tobacco + paper"),
                4, 4, 0x404040, false);
    }
}