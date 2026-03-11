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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

public class WoodenPipeRecipeCategory implements IRecipeCategory<WoodenPipeJeiRecipe> {

    public static final RecipeType<WoodenPipeJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "wooden_pipe"), WoodenPipeJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public WoodenPipeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(116, 76);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<WoodenPipeJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Wooden Pipe");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WoodenPipeJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, gridX(2), gridY(0))
                .addIngredients(net.minecraft.world.item.crafting.Ingredient.of(ItemTags.PLANKS));

        builder.addSlot(RecipeIngredientRole.INPUT, gridX(1), gridY(1))
                .addIngredients(net.minecraft.world.item.crafting.Ingredient.of(ItemTags.PLANKS));

        builder.addSlot(RecipeIngredientRole.INPUT, gridX(2), gridY(1))
                .addItemStack(recipe.stick());

        builder.addSlot(RecipeIngredientRole.INPUT, gridX(0), gridY(2))
                .addItemStack(recipe.stick());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 30)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(WoodenPipeJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                slot.draw(guiGraphics, gridX(x) - 1, gridY(y) - 1);
            }
        }

        slot.draw(guiGraphics, 93, 29);

        guiGraphics.drawString(mc.font,
                Component.literal("Any Planks"),
                4, 4, 0x404040, false);
    }

    private static int gridX(int x) {
        return 4 + (x * 18);
    }

    private static int gridY(int y) {
        return 16 + (y * 18);
    }
}