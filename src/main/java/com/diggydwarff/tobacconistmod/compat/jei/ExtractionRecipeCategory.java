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
import com.diggydwarff.tobacconistmod.backport.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

public final class ExtractionRecipeCategory implements IRecipeCategory<ExtractionRecipe> {
    public static final RecipeType<ExtractionRecipe> TYPE = new RecipeType<>(new ResourceLocation(TobacconistMod.MODID, "extraction"), ExtractionRecipe.class);
    private final IDrawable icon, slot, background;
    public ExtractionRecipeCategory(IGuiHelper helper) { icon = helper.createDrawableItemStack(new ItemStack(net.minecraft.world.level.block.Blocks.CAULDRON)); slot = helper.getSlotDrawable(); background = helper.createBlankDrawable(190, 66); }
    @Override public RecipeType<ExtractionRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("tobacconistmod.vapor.heated_cauldron"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 190; }
    @Override public int getHeight() { return 66; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, ExtractionRecipe recipe, IFocusGroup focuses) {
        int index = 0;
        for (var input : recipe.inputs()) {
            if (water(input)) continue;
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + index++ * 25, 8)
                    .addItemStacks(Arrays.stream(input.ingredient().getItems()).map(stack -> com.diggydwarff.tobacconistmod.backport.BackportStacks.copyWithCount(stack, input.count())).toList());
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 110, 8).addItemStack(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 163, 8).addItemStack(recipe.result());
    }
    private static boolean water(ExtractionRecipe.Input input) {
        return input.ingredient().test(com.diggydwarff.tobacconistmod.backport.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
    }
    @Override public void draw(ExtractionRecipe recipe, IRecipeSlotsView slots, com.mojang.blaze3d.vertex.PoseStack backportPose, double mouseX, double mouseY) {
        GuiGraphics gui = new GuiGraphics(backportPose);
        for (int i = 0; i < recipe.inputs().stream().filter(input -> !water(input)).count(); i++) slot.draw(gui.pose(), 4 + i * 25, 7);
        slot.draw(gui.pose(), 109, 7); slot.draw(gui.pose(), 162, 7);
        gui.drawString(Minecraft.getInstance().font, Component.literal("→"), 140, 12, 0x404040, false);
        gui.drawString(Minecraft.getInstance().font, Component.translatable("tobacconistmod.vapor.jei_extraction", recipe.duration() / 20), 4, 36, 0x404040, false);
        gui.drawString(Minecraft.getInstance().font, Component.translatable("tobacconistmod.vapor.cauldron_jei_hint"), 4, 50, 0x404040, false);
    }
}
