package com.diggydwarff.tobacconistmod.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationListScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Presentation-only extension of NeoForge's validated, undoable configuration editor. */
final class TobacconistConfigSectionScreen extends ConfigurationSectionScreen {
  TobacconistConfigSectionScreen(
      Screen parent, ModConfig.Type type, ModConfig config, Component title) {
    super(parent, type, config, title);
  }

  private TobacconistConfigSectionScreen(
      Context parentContext,
      Screen parent,
      UnmodifiableConfig spec,
      String key,
      UnmodifiableConfig values,
      Component title) {
    super(parentContext, parent, spec.valueMap(), key, values.entrySet(), title);
  }

  @Override
  protected void addTitle() {
    prepareLayout(this);
  }

  @Override
  protected void addContents() {
    list = layout.addToContents(new TobacconistOptionsList(minecraft, width, this));
    addOptions();
  }

  @Override
  protected void repositionElements() {
    fitFooter(width, undoButton, resetButton, doneButton);
    super.repositionElements();
  }

  @Override
  protected MutableComponent getTranslationComponent(String key) {
    return readable(getTranslationKey(key), key);
  }

  static MutableComponent readable(String translation, String key) {
    if (I18n.exists(translation)) return Component.translatable(translation);
    String words = key.replace('_', ' ').replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
    return Component.literal(Character.toUpperCase(words.charAt(0)) + words.substring(1));
  }

  @Override
  protected Element createSection(String key, UnmodifiableConfig spec, UnmodifiableConfig values) {
    if (spec.isEmpty()) return null;
    return new Element(
        getTranslationComponent(key),
        getTooltipComponent(key, null),
        Button.builder(
                Component.translatable("gui.tobacconistmod.config.edit"),
                button -> {
                  var next =
                      (TobacconistConfigSectionScreen)
                          sectionCache.computeIfAbsent(
                              key,
                              ignored ->
                                  new TobacconistConfigSectionScreen(
                                      context,
                                      this,
                                      spec,
                                      key,
                                      values,
                                      getTranslationComponent(key)));
                  next.refresh();
                  minecraft.setScreen(next);
                })
            .tooltip(Tooltip.create(getTooltipComponent(key, null)))
            .build(),
        false);
  }

  @Override
  protected <T> Element createList(
      String key, ModConfigSpec.ListValueSpec spec, ModConfigSpec.ConfigValue<List<T>> value) {
    return new Element(
        getTranslationComponent(key),
        getTooltipComponent(key, null),
        Button.builder(
                Component.translatable("gui.tobacconistmod.config.edit"),
                button -> {
                  var next =
                      sectionCache.computeIfAbsent(
                          key,
                          ignored ->
                              new TobacconistListScreen<>(
                                  Context.list(context, this),
                                  key,
                                  getTranslationComponent(key),
                                  spec,
                                  value));
                  ((TobacconistListScreen<?>) next).refresh();
                  minecraft.setScreen(next);
                })
            .tooltip(Tooltip.create(getTooltipComponent(key, null)))
            .build(),
        false);
  }

  private void refresh() {
    rebuild();
  }

  @Override
  public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
    TobacconistConfigStyle.editor(g, width, height, title);
  }

  @Override
  public void onClose() {
    super.onClose();
    // Let the generic owner apply its ordinary restart/reload handling, then return to the hub.
    if (lastScreen instanceof ConfigurationScreen owner) owner.onClose();
  }

  private static void prepareLayout(OptionsSubScreen screen) {
    screen.layout.setHeaderHeight(50);
    screen.layout.setFooterHeight(36);
  }

  private static void fitFooter(int width, Button undo, Button reset, Button done) {
    int count = 1 + (undo == null ? 0 : 1) + (reset == null ? 0 : 1);
    int buttonWidth = Math.min(Button.SMALL_WIDTH, (width - 24 - 8 * (count - 1)) / count);
    for (Button button : new Button[] {undo, reset, done})
      if (button != null) button.setWidth(buttonWidth);
  }

  private static final class TobacconistOptionsList extends OptionsList {
    TobacconistOptionsList(Minecraft mc, int width, OptionsSubScreen screen) {
      super(mc, width, screen);
    }

    @Override
    protected void renderListBackground(GuiGraphics g) {
      g.fill(width / 2 - 157, getY(), width / 2 + 157, getBottom(), 0x324C3525);
    }

    @Override
    protected void renderListSeparators(GuiGraphics g) {
      g.fill(width / 2 - 157, getY() - 1, width / 2 + 157, getY(), 0xFF82643D);
      g.fill(width / 2 - 157, getBottom(), width / 2 + 157, getBottom() + 1, 0xFF82643D);
    }
  }

  private static final class TobacconistListScreen<T> extends ConfigurationListScreen<T> {
    TobacconistListScreen(
        Context context,
        String key,
        Component title,
        ModConfigSpec.ListValueSpec spec,
        ModConfigSpec.ConfigValue<List<T>> value) {
      super(context, key, title, spec, value);
    }

    @Override
    protected void addTitle() {
      prepareLayout(this);
    }

    @Override
    protected void addContents() {
      list = layout.addToContents(new TobacconistOptionsList(minecraft, width, this));
      addOptions();
    }

    @Override
    protected void repositionElements() {
      fitFooter(width, undoButton, resetButton, doneButton);
      super.repositionElements();
    }

    @Override
    protected MutableComponent getTranslationComponent(String key) {
      return readable(getTranslationKey(key), key);
    }

    void refresh() {
      rebuild();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
      TobacconistConfigStyle.editor(g, width, height, title);
    }
  }
}
