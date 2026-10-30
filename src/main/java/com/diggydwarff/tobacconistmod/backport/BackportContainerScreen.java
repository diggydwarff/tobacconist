package com.diggydwarff.tobacconistmod.backport;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Bridges screen entry points while retaining the existing layouts and slot coordinates. */
public abstract class BackportContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected BackportContainerScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    protected abstract void renderBg(GuiGraphics gui, float partial, int mouseX, int mouseY);
    @Override protected final void renderBg(PoseStack pose, float partial, int mouseX, int mouseY) {
        renderBg(new GuiGraphics(pose), partial, mouseX, mouseY);
    }
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) { super.renderLabels(gui.pose(), mouseX, mouseY); }
    @Override protected final void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        renderLabels(new GuiGraphics(pose), mouseX, mouseY);
    }
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partial) {
        super.render(gui.pose(), mouseX, mouseY, partial);
        for (var child : children()) {
            if (child instanceof BackportButton button && button.visible && button.isHoveredOrFocused()
                    && button.getTooltip() != null) {
                gui.renderTooltip(font, button.getTooltip().text(), mouseX, mouseY);
            }
        }
    }
    @Override public final void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        render(new GuiGraphics(pose), mouseX, mouseY, partial);
    }
    public void renderBackground(GuiGraphics gui) { super.renderBackground(gui.pose()); }
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) { super.renderTooltip(gui.pose(), mouseX, mouseY); }
}
