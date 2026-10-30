package com.diggydwarff.tobacconistmod.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Tobacco cabinet, aged brass and parchment; works with or without Create. */
final class TobacconistConfigStyle {
  static final int INK = 0xFF201B16;
  static final int PANEL = 0xFF30271E;
  static final int BRASS = 0xFFB99A5D;
  static final int TEXT = 0xFFE8DCC1;
  static final int MUTED = 0xFFAD9B7D;
  static final int ACCENT = 0xFFC89651;

  static void backdrop(GuiGraphics g, int width, int height) {
    g.fillGradient(0, 0, width, height, 0xFF251E17, 0xFF0E100C);
    for (int x = 12; x < width; x += 48) {
      g.fill(x, 0, x + 1, height, 0x163F3324);
      g.fill(x + 2, 0, x + 3, height, 0x0C8D6B3E);
    }
  }

  static void frame(GuiGraphics g, int x, int y, int w, int h) {
    g.fill(x, y, x + w, y + h, 0xFF130F0C);
    g.fill(x + 1, y + 1, x + w - 1, y + h - 1, BRASS);
    g.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFF51402B);
    for (int yy = y + 23; yy < y + h - 5; yy += 9) g.fill(x + 4, yy, x + w - 4, yy + 1, 0xFF493824);
    g.fill(x + 7, y + 23, x + w - 7, y + h - 7, INK);
    g.fill(x + 3, y + 3, x + w - 3, y + 21, 0xFFAA8B52);
    g.fill(x + 4, y + 4, x + w - 4, y + 5, 0xFFD1B77C);
    g.fill(x + 4, y + 20, x + w - 4, y + 21, 0xFF6E542F);
    for (int xx : new int[] {x + 5, x + w - 8})
      for (int yy : new int[] {y + 8, y + h - 15}) {
        g.fill(xx, yy, xx + 3, yy + 3, 0xFF211B12);
        g.fill(xx, yy, xx + 2, yy + 2, BRASS);
      }
  }

  static String fit(String text, int width) {
    var font = Minecraft.getInstance().font;
    if (font.width(text) <= width) return text;
    int available = width - font.width("...");
    return available > 0
        ? font.plainSubstrByWidth(text, available) + "..."
        : font.plainSubstrByWidth(text, Math.max(0, width));
  }

  static void title(GuiGraphics g, Component title, int centerX, int y, int maxWidth, int color) {
    var font = Minecraft.getInstance().font;
    String label = fit(title.getString(), maxWidth);
    g.drawString(font, label, centerX - font.width(label) / 2, y, color, false);
  }

  static void editor(GuiGraphics g, int width, int height, Component section) {
    backdrop(g, width, height);
    int panelWidth = Math.min(436, width - 8);
    frame(g, (width - panelWidth) / 2, 5, panelWidth, height - 10);
    title(
        g,
        Component.translatable("gui.tobacconistmod.config.title"),
        width / 2,
        12,
        panelWidth - 24,
        0xFF281C11);
    title(g, section, width / 2, 34, panelWidth - 24, TEXT);
  }

  private TobacconistConfigStyle() {}
}
