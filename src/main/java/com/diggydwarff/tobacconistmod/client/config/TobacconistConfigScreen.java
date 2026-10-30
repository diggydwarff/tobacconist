package com.diggydwarff.tobacconistmod.client.config;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.novelty.NoveltyConfig;
import java.util.EnumMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Themed entrance to all four existing configuration files. No optional GUI dependency. */
public final class TobacconistConfigScreen extends Screen {
  enum Area {
    CLIENT(ModConfig.Type.CLIENT, TobacconistConfig.CLIENT_SPEC),
    COMMON(ModConfig.Type.COMMON, TobacconistConfig.COMMON_SPEC),
    WORLD(ModConfig.Type.SERVER, TobacconistConfig.SERVER_SPEC),
    NOVELTIES(ModConfig.Type.SERVER, NoveltyConfig.SPEC);

    final ModConfig.Type type;
    final ModConfigSpec spec;

    Area(ModConfig.Type type, ModConfigSpec spec) {
      this.type = type;
      this.spec = spec;
    }

    String key() {
      return name().toLowerCase(java.util.Locale.ROOT);
    }

    Component label() {
      return text(key());
    }

    ItemStack icon() {
      return new ItemStack(
          switch (this) {
            case CLIENT -> ModItems.TOBACCONISTS_SPECTACLES.get();
            case COMMON -> ModItems.WOODEN_SMOKING_PIPE.get();
            case WORLD -> Items.POTION;
            case NOVELTIES -> ModItems.KISERU_SMOKING_PIPE.get();
          });
    }
  }

  private final Screen parent;
  private final ModContainer mod;
  private final ConfigurationScreen editorOwner;
  private final EnumMap<Area, TobacconistConfigButton> buttons = new EnumMap<>(Area.class);
  private int left, top, panelWidth, panelHeight, firstY, cardHeight;

  public TobacconistConfigScreen(ModContainer mod, Screen parent) {
    super(text("title"));
    this.mod = mod;
    this.parent = parent;
    editorOwner = new ConfigurationScreen(mod, this, TobacconistConfigSectionScreen::new);
  }

  static Component text(String key) {
    return Component.translatable("gui.tobacconistmod.config." + key);
  }

  @Override
  protected void init() {
    buttons.clear();
    panelWidth = Math.min(440, width - 16);
    panelHeight = Math.min(344, height - 16);
    left = (width - panelWidth) / 2;
    top = (height - panelHeight) / 2;
    firstY = height < 300 ? 50 : 66;
    cardHeight = (panelHeight - firstY - 70) / 4;
    for (Area area : Area.values()) {
      int y = top + firstY + area.ordinal() * (cardHeight + 6);
      buttons.put(
          area,
          addRenderableWidget(
              new TobacconistConfigButton(
                  left + panelWidth - 78,
                  y + (cardHeight - 18) / 2,
                  62,
                  area.label(),
                  text("open"),
                  button -> open(area))));
    }
    Component done = Component.translatable("gui.done");
    addRenderableWidget(
        new TobacconistConfigButton(
            left + panelWidth - 88, top + panelHeight - 27, 72, done, done, button -> onClose()));
    updateAvailability();
  }

  private ModConfig config(Area area) {
    // There are TWO server configs. Match the spec, never the first config of a type.
    return ModConfigs.getConfigSet(area.type).stream()
        .filter(c -> c.getModId().equals(mod.getModId()) && c.getSpec() == area.spec)
        .findFirst()
        .orElse(null);
  }

  Component unavailable(Area area) {
    if (config(area) == null || !area.spec.isLoaded())
      return text(area.type == ModConfig.Type.SERVER ? "world_unloaded" : "unloaded");
    if (area.type == ModConfig.Type.SERVER
        && minecraft.getCurrentServer() != null
        && !minecraft.isSingleplayer())
      return ConfigurationScreen.TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE;
    if (area.type == ModConfig.Type.SERVER
        && minecraft.hasSingleplayerServer()
        && minecraft.getSingleplayerServer().isPublished())
      return ConfigurationScreen.TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN;
    return null;
  }

  private void updateAvailability() {
    for (var entry : buttons.entrySet()) {
      Component reason = unavailable(entry.getKey());
      entry.getValue().active = reason == null;
      entry
          .getValue()
          .setTooltip(
              Tooltip.create(
                  reason == null ? text(entry.getKey().key() + "_description") : reason));
    }
  }

  private void open(Area area) {
    if (unavailable(area) != null) return;
    minecraft.setScreen(
        new TobacconistConfigSectionScreen(editorOwner, area.type, config(area), area.label()));
  }

  @Override
  public void tick() {
    updateAvailability();
  }

  @Override
  public void onClose() {
    minecraft.setScreen(parent);
  }

  @Override
  public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
    TobacconistConfigStyle.backdrop(g, width, height);
    TobacconistConfigStyle.frame(g, left, top, panelWidth, panelHeight);
    TobacconistConfigStyle.title(g, title, width / 2, top + 8, panelWidth - 30, 0xFF281C11);
    g.drawString(
        font,
        TobacconistConfigStyle.fit(text("subtitle").getString(), panelWidth - 32),
        left + 16,
        top + 33,
        TobacconistConfigStyle.TEXT,
        false);
    if (firstY > 50)
      g.drawString(
          font,
          TobacconistConfigStyle.fit(text("description").getString(), panelWidth - 32),
          left + 16,
          top + 48,
          TobacconistConfigStyle.MUTED,
          false);
    for (Area area : Area.values()) card(g, area);
    g.drawString(
        font,
        TobacconistConfigStyle.fit(text("saving").getString(), panelWidth - 32),
        left + 16,
        top + panelHeight - 44,
        TobacconistConfigStyle.MUTED,
        false);
  }

  private void card(GuiGraphics g, Area area) {
    var button = buttons.get(area);
    int y = top + firstY + area.ordinal() * (cardHeight + 6);
    g.fill(left + 12, y, left + panelWidth - 12, y + cardHeight, TobacconistConfigStyle.PANEL);
    g.fill(
        left + 12,
        y,
        left + panelWidth - 12,
        y + 1,
        button.isHoveredOrFocused() ? TobacconistConfigStyle.ACCENT : 0xFF614D32);
    g.fill(left + 12, y + 4, left + 14, y + cardHeight - 4, TobacconistConfigStyle.BRASS);
    g.renderItem(area.icon(), left + 20, y + (cardHeight - 16) / 2);
    int textX = left + 44, textWidth = panelWidth - 128;
    g.drawString(
        font,
        TobacconistConfigStyle.fit(area.label().getString(), textWidth),
        textX,
        y + (cardHeight - 22) / 2,
        button.active ? TobacconistConfigStyle.TEXT : TobacconistConfigStyle.MUTED,
        false);
    Component description =
        !button.active && area.type == ModConfig.Type.SERVER
            ? text("world_locked")
            : text(area.key() + "_description");
    g.drawString(
        font,
        TobacconistConfigStyle.fit(description.getString(), textWidth),
        textX,
        y + (cardHeight - 22) / 2 + 13,
        TobacconistConfigStyle.MUTED,
        false);
  }
}
