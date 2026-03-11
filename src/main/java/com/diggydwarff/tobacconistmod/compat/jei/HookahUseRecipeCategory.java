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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class HookahUseRecipeCategory implements IRecipeCategory<HookahUseJeiRecipe> {

    public static final RecipeType<HookahUseJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "hookah_use"), HookahUseJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public HookahUseRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.HOOKAH.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<HookahUseJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Hookah Use");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HookahUseJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 6)
                .addItemStack(recipe.loadedHookah());
    }

    @Override
    public void draw(HookahUseJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 5);

        guiGraphics.drawString(mc.font, recipe.line1(), 6, 34, 0x404040, false);
        guiGraphics.drawString(mc.font, recipe.line2(), 6, 48, 0x808080, false);
    }
}