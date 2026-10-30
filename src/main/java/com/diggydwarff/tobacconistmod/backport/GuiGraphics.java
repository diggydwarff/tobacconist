package com.diggydwarff.tobacconistmod.backport;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Small client adapter for the PoseStack GUI API used by Minecraft 1.19.2. */
public final class GuiGraphics extends GuiComponent {
    private final PoseStack pose;

    public GuiGraphics(PoseStack pose) { this.pose = pose; }
    public PoseStack pose() { return pose; }
    public int guiWidth() { return Minecraft.getInstance().getWindow().getGuiScaledWidth(); }
    public int guiHeight() { return Minecraft.getInstance().getWindow().getGuiScaledHeight(); }
    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }
    public void fillGradient(int x1, int y1, int x2, int y2, int start, int end) {
        super.fillGradient(pose, x1, y1, x2, y2, start, end);
    }
    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }
    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }
    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        blit(texture, x, y, u, v, width, height, 256, 256);
    }
    public void blit(ResourceLocation texture, int x, int y, float u, float v,
                     int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(pose, x, y, u, v, width, height, textureWidth, textureHeight);
    }
    public void blit(ResourceLocation texture, int x, int y, int width, int height, float u, float v,
                     int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(pose, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
    public void renderItem(ItemStack stack, int x, int y) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    public void renderFakeItem(ItemStack stack, int x, int y) { renderItem(stack, x, y); }
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        renderItemDecorations(font, stack, x, y, null);
    }
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String count) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y, count);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    public void renderTooltip(Font font, Component text, int x, int y) {
        if (Minecraft.getInstance().screen != null) Minecraft.getInstance().screen.renderTooltip(pose, text, x, y);
    }
    public void renderTooltip(Font font, ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) mc.screen.renderTooltip(pose,
                stack.getTooltipLines(mc.player, mc.options.advancedItemTooltips
                        ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL),
                stack.getTooltipImage(), x, y, font, stack);
    }
    public void renderComponentTooltip(Font font, List<Component> lines, int x, int y) {
        if (Minecraft.getInstance().screen != null) Minecraft.getInstance().screen.renderComponentTooltip(pose, lines, x, y);
    }
}
