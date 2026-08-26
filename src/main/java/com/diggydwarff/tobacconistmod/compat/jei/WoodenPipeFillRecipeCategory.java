package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
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

public class WoodenPipeFillRecipeCategory implements IRecipeCategory<WoodenPipeFillJeiRecipe> {

    public static final RecipeType<WoodenPipeFillJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "wooden_pipe_fill"), WoodenPipeFillJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public WoodenPipeFillRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(116, 60);
        this.icon = guiHelper.createDrawableItemStack(makeWoodPipe("minecraft:birch_planks"));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<WoodenPipeFillJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tobacconistmod.jei.fill_wooden_pipe");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WoodenPipeFillJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 20)
                .addItemStacks(recipe.tobaccos());

        builder.addSlot(RecipeIngredientRole.INPUT, 32, 20)
                .addItemStacks(recipe.emptyPipes());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 20)
                .addItemStacks(recipe.filledPipes());
    }

    @Override
    public void draw(WoodenPipeFillJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 9, 19);
        slot.draw(guiGraphics, 31, 19);
        slot.draw(guiGraphics, 85, 19);

        guiGraphics.drawString(mc.font,
                Component.translatable("tobacconistmod.jei.tobacco_plus_pipe"),
                4, 4, 0x404040, false);

        guiGraphics.drawString(mc.font,
                Component.translatable("tobacconistmod.jei.offhand_use"),
                4, 44, 0x808080, false);
    }

    private static ItemStack makeWoodPipe(String plankId) {
        ItemStack pipe = new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get());

        LegacyItemTags.getOrCreateTag(pipe).putString(
                WoodenPipeRecipe.NBT_WOOD_PLANK,
                plankId
        );

        return pipe;
    }
}