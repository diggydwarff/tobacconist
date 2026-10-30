package com.diggydwarff.tobacconistmod.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Native keyboard/narration behavior with brass cabinet hardware. */
final class TobacconistConfigButton extends Button {
  private final Component caption;

  TobacconistConfigButton(
      int x, int y, int width, Component message, Component caption, OnPress action) {
    super(x, y, width, 18, message, action, DEFAULT_NARRATION);
    this.caption = caption;
  }

  @Override
  public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
    int x = getX(), y = getY(), w = getWidth();
    g.fill(x, y, x + w, y + height, 0xFF17130F);
    g.fill(
        x + 1,
        y + 1,
        x + w - 1,
        y + height - 1,
        !active ? 0xFF514A3B : isHoveredOrFocused() ? 0xFFD4B576 : TobacconistConfigStyle.BRASS);
    g.fill(x + 2, y + 2, x + w - 2, y + 3, active ? 0xFFE1C792 : 0xFF645B49);
    g.fill(x + 2, y + height - 3, x + w - 2, y + height - 2, 0xFF715832);
    if (isFocused()) g.renderOutline(x, y, w, height, TobacconistConfigStyle.TEXT);
    var font = Minecraft.getInstance().font;
    String text = TobacconistConfigStyle.fit(caption.getString(), w - 6);
    g.drawString(
        font, text, x + (w - font.width(text)) / 2, y + 5, active ? 0xFF261E14 : 0xFFAAA08C, false);
  }
}
