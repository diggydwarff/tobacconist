package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
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

public final class FlavorMolassesRecipeCategory implements IRecipeCategory<FlavorMolassesJeiRecipe> {
    public static final RecipeType<FlavorMolassesJeiRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "flavor_molasses"),
            FlavorMolassesJeiRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public FlavorMolassesRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(142, 58);
        this.icon = guiHelper.createDrawableItemStack(
                BottledMolassesFlavors.BOTTLED_MOLASSES_APPLE_FLAVOR.getStack()
        );
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<FlavorMolassesJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tobacconistmod.jei.flavor_molasses");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FlavorMolassesJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 22)
                .addItemStack(recipe.plainMolasses());
        builder.addSlot(RecipeIngredientRole.INPUT, 42, 22)
                .addItemStack(recipe.essence());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 22)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(FlavorMolassesJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        slot.draw(guiGraphics, 11, 21);
        slot.draw(guiGraphics, 41, 21);
        slot.draw(guiGraphics, 107, 21);
        guiGraphics.drawString(mc.font, "+", 33, 27, 0x404040, false);
        guiGraphics.drawString(mc.font, ">", 78, 27, 0x404040, false);
        guiGraphics.drawString(mc.font,
                Component.translatable("tobacconistmod.jei.flavor_molasses.line"),
                4, 4, 0x404040, false);
        guiGraphics.drawString(mc.font,
                Component.translatable("tobacconistmod.jei.returns_glass_bottle"),
                4, 49, 0x808080, false);
    }
}
