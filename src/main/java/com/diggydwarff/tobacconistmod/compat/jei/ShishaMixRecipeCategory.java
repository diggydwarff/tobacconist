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

public class ShishaMixRecipeCategory implements IRecipeCategory<ShishaMixJeiRecipe> {

    public static final RecipeType<ShishaMixJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "shisha_mix"), ShishaMixJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public ShishaMixRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.SHISHA_TOBACCO.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<ShishaMixJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Shisha Mixing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ShishaMixJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 22)
                .addItemStacks(recipe.tobaccos());

        builder.addSlot(RecipeIngredientRole.INPUT, 32, 22)
                .addItemStacks(recipe.flavorings());

        builder.addSlot(RecipeIngredientRole.INPUT, 54, 22)
                .addItemStacks(recipe.flavorings());

        builder.addSlot(RecipeIngredientRole.INPUT, 76, 22)
                .addItemStacks(recipe.flavorings());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 126, 22)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ShishaMixJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 21);
        slot.draw(guiGraphics, 31, 21);
        slot.draw(guiGraphics, 53, 21);
        slot.draw(guiGraphics, 75, 21);
        slot.draw(guiGraphics, 125, 21);

        guiGraphics.drawString(mc.font, "Loose tobacco + 0-3 flavors", 4, 4, 0x404040, false);
        guiGraphics.drawString(mc.font, "Extra flavor slots optional", 4, 52, 0x808080, false);
    }
}