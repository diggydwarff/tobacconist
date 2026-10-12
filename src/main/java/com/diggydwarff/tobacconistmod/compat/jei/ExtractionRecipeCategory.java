package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.vapor.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

public final class ExtractionRecipeCategory implements IRecipeCategory<ExtractionRecipe> {
    public static final RecipeType<ExtractionRecipe> TYPE = new RecipeType<>(ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "extraction"), ExtractionRecipe.class);
    private final IDrawable icon, slot;
    public ExtractionRecipeCategory(IGuiHelper helper) { icon = helper.createDrawableItemStack(new ItemStack(VaporContent.EXTRACTION_STATION.get())); slot = helper.getSlotDrawable(); }
    @Override public RecipeType<ExtractionRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.tobacconistmod.extraction_station"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 148; }
    @Override public int getHeight() { return 52; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, ExtractionRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            var input = recipe.inputs().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + i * 25, 8)
                    .addItemStacks(Arrays.stream(input.ingredient().getItems()).map(stack -> stack.copyWithCount(input.count())).toList());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 121, 8).addItemStack(recipe.result());
    }
    @Override public void draw(ExtractionRecipe recipe, IRecipeSlotsView slots, GuiGraphics gui, double mouseX, double mouseY) {
        for (int i = 0; i < recipe.inputs().size(); i++) slot.draw(gui, 4 + i * 25, 7);
        slot.draw(gui, 120, 7);
        gui.drawString(Minecraft.getInstance().font, Component.literal("→"), 91, 12, 0x404040, false);
        gui.drawString(Minecraft.getInstance().font, Component.translatable("tobacconistmod.vapor.jei_extraction", recipe.duration() / 20), 4, 36, 0x404040, false);
    }
}
