package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import com.diggydwarff.tobacconistmod.backport.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TobaccoBarrelRecipeCategory implements IRecipeCategory<BarrelProcessJeiRecipe> {

    public static final RecipeType<BarrelProcessJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "tobacco_barrel"), BarrelProcessJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public TobaccoBarrelRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(190, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.TOBACCO_BARREL.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<BarrelProcessJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tobacconistmod.jei.tobacco_barrel_processing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BarrelProcessJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 6)
                .addItemStack(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 6)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(BarrelProcessJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, com.mojang.blaze3d.vertex.PoseStack backportPose,
                     double mouseX, double mouseY) {
        GuiGraphics guiGraphics = new GuiGraphics(backportPose);
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics.pose(), 21, 5);
        slot.draw(guiGraphics.pose(), 129, 5);

        guiGraphics.drawString(mc.font, ">", 82, 12, 0x404040, false);

        guiGraphics.drawString(mc.font, recipe.line1(), 6, 34, 0x404040, false);
        guiGraphics.drawString(mc.font, recipe.line2(), 6, 48, 0x808080, false);
    }
}