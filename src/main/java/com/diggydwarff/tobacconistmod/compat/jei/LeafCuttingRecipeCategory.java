package com.diggydwarff.tobacconistmod.compat.jei;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
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

import java.util.List;

public class LeafCuttingRecipeCategory implements IRecipeCategory<LeafCuttingJeiRecipe> {

    public static final RecipeType<LeafCuttingJeiRecipe> TYPE =
            new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "leaf_cutting"), LeafCuttingJeiRecipe.class);

    private static final List<ItemStack> CHAVETAS = List.of(
            new ItemStack(ModItems.STONE_CHAVETA.get()),
            new ItemStack(ModItems.COPPER_CHAVETA.get()),
            new ItemStack(ModItems.IRON_CHAVETA.get()),
            new ItemStack(ModItems.GOLD_CHAVETA.get()),
            new ItemStack(ModItems.DIAMOND_CHAVETA.get()),
            new ItemStack(ModItems.NETHERITE_CHAVETA.get())
    );

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public LeafCuttingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(116, 76);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.IRON_CHAVETA.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<LeafCuttingJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Leaf Cutting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, LeafCuttingJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, gridX(recipe.leafX()), gridY(recipe.leafY()))
                .addItemStack(recipe.inputLeaf());

        builder.addSlot(RecipeIngredientRole.INPUT, gridX(recipe.chavetaX()), gridY(recipe.chavetaY()))
                .addItemStacks(CHAVETAS);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 30)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(LeafCuttingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                slot.draw(guiGraphics, gridX(x) - 1, gridY(y) - 1);
            }
        }

        slot.draw(guiGraphics, 93, 29);

        guiGraphics.drawString(mc.font,
                Component.literal(TobaccoCuringHelper.getCutDisplayName(recipe.cutType())),
                4, 4, 0x404040, false);
    }

    private static int gridX(int x) {
        return 4 + (x * 18);
    }

    private static int gridY(int y) {
        return 16 + (y * 18);
    }
}