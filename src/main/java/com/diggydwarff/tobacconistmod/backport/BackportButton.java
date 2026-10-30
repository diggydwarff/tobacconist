package com.diggydwarff.tobacconistmod.backport;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Retains the custom controls, including keyboard narration, on the legacy widget API. */
public abstract class BackportButton extends Button {
    private Tooltip tooltip;
    protected BackportButton(int x, int y, int width, int height, Component label, OnPress action) {
        super(x, y, width, height, label, action);
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setTooltip(Tooltip tooltip) { this.tooltip = tooltip; }
    public Tooltip getTooltip() { return tooltip; }
    protected abstract void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partial);
    @Override public final void renderButton(PoseStack pose, int mouseX, int mouseY, float partial) {
        renderWidget(new GuiGraphics(pose), mouseX, mouseY, partial);
    }
}
