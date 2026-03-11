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

public class HookahStationRecipeCategory implements IRecipeCategory<HookahStationJeiRecipe> {

    public static final RecipeType<HookahStationJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "hookah_station"), HookahStationJeiRecipe.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TobacconistMod.MODID, "textures/gui/hookah_gui.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public HookahStationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 166);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.HOOKAH.get().asItem()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<HookahStationJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Hookah Station");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HookahStationJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 36)
                .addItemStacks(recipe.hookahs());

        builder.addSlot(RecipeIngredientRole.INPUT, 53, 36)
                .addItemStacks(recipe.fuels());

        builder.addSlot(RecipeIngredientRole.INPUT, 86, 15)
                .addItemStack(recipe.topSlot());

        builder.addSlot(RecipeIngredientRole.INPUT, 86, 60)
                .addItemStack(recipe.bottomSlot());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 300, 300)
                .addItemStacks(recipe.outputs());
    }

    @Override
    public void draw(HookahStationJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        slot.draw(guiGraphics, 17, 35);

        guiGraphics.drawString(mc.font, "Load hookah with fuel, water,", 8, -44, 0x404040, false);
        guiGraphics.drawString(mc.font, "and shisha.", 8, -36, 0x404040, false);
        guiGraphics.drawString(mc.font, "Then right-click to smoke.", 8, -24, 0x808080, false);
    }
}