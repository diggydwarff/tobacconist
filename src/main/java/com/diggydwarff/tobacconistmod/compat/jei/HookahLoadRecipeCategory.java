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

public class HookahLoadRecipeCategory implements IRecipeCategory<HookahLoadJeiRecipe> {

    public static final RecipeType<HookahLoadJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "hookah_load"), HookahLoadJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public HookahLoadRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.HOOKAH.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<HookahLoadJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Hookah Loading");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HookahLoadJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 6).addItemStack(recipe.hookah());
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 6).addItemStack(recipe.shisha());
        builder.addSlot(RecipeIngredientRole.INPUT, 54, 6).addItemStack(recipe.water());
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 6).addItemStack(recipe.heat());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 6).addItemStack(recipe.output());
    }

    @Override
    public void draw(HookahLoadJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 5);
        slot.draw(guiGraphics, 31, 5);
        slot.draw(guiGraphics, 53, 5);
        slot.draw(guiGraphics, 75, 5);
        slot.draw(guiGraphics, 139, 5);

        guiGraphics.drawString(mc.font, ">", 112, 12, 0x404040, false);
        guiGraphics.drawString(mc.font, recipe.line1(), 6, 34, 0x404040, false);
        guiGraphics.drawString(mc.font, recipe.line2(), 6, 48, 0x808080, false);
    }
}