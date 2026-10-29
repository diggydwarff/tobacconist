package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/** Create controls when installed, with local monitor textures for standalone play. */
final class VaporGui {
    static final ResourceLocation BENCH = texture("vapor_blending_bench");
    static final ResourceLocation LEGACY = texture("vapor_legacy_extraction");
    static final ResourceLocation PEN = texture("vapor_pen");
    static final ResourceLocation BOX = texture("vapor_box");
    private static final ResourceLocation INVENTORY = texture("player_inventory");
    private static final ResourceLocation SELECTOR = texture("production_monitor_widgets");
    private static final ResourceLocation VALUE_FRAMES = texture("vapor_selectors");
    private static final ResourceLocation WIDGETS = new ResourceLocation("create", "textures/gui/widgets.png");
    private static final ResourceLocation ICONS = new ResourceLocation("create", "textures/gui/icons.png");
    static final int TEXT = 0xE3DACE;
    static final int TITLE = 0x592424;

    private VaporGui() {}

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(TobacconistMod.MODID, "textures/gui/" + name + ".png");
    }

    static void panel(GuiGraphics gui, ResourceLocation texture, int x, int y, int width, int height) {
        gui.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    static void inventory(GuiGraphics gui, int x, int y) {
        gui.blit(INVENTORY, x, y, 0, 0, 176, 108, 256, 256);
    }

    static final class Control extends Button {
        private final Supplier<Component> label;
        private final Runnable secondary;
        private final int iconX;
        private final int iconY;
        private final boolean selector;
        private final Supplier<Boolean> selected;

        Control(int x, int y, int width, Supplier<Component> label, Runnable action, Runnable secondary) {
            this(x, y, width, label, action, secondary, false);
        }

        Control(int x, int y, int width, Supplier<Component> label, Runnable action, Runnable secondary, boolean selector) {
            this(x, y, width, label, action, secondary, -1, -1, selector, () -> false);
        }

        Control(int x, int y, int iconX, int iconY, Component name, Runnable action, Supplier<Boolean> selected) {
            this(x, y, 18, () -> name, action, null, iconX, iconY, false, selected);
        }

        private Control(int x, int y, int width, Supplier<Component> label, Runnable action, Runnable secondary,
                        int iconX, int iconY, boolean selector, Supplier<Boolean> selected) {
            super(x, y, width, selector ? 22 : 18, label.get(), ignored -> action.run(), DEFAULT_NARRATION);
            this.label = label;
            this.secondary = secondary;
            this.iconX = iconX;
            this.iconY = iconY;
            this.selector = selector;
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partial) {
            setMessage(label.get());
            if (selector) drawSelector(gui);
            else drawAction(gui);
            if (iconX >= 0 && CreateCompat.loaded()) {
                int u = iconY == 80 && selected.get() ? 16 : iconX;
                gui.blit(ICONS, getX() + 1, getY() + 1, u, iconY, 16, 16, 256, 256);
                return;
            }
            String message = getMessage().getString();
            if (iconX >= 0) message = iconX == 16 ? "x" : iconY == 80 ? selected.get() ? "II" : ">" : message.substring(0, 1);
            var font = Minecraft.getInstance().font;
            String visible = font.plainSubstrByWidth(message, getWidth() - 6);
            // The light Create button face needs dark, shadow-free text in both states.
            int color = active ? 0x202020 : 0x303030;
            gui.drawString(font, visible, getX() + (getWidth() - font.width(visible)) / 2,
                    getY() + (getHeight() - 8) / 2, color, false);
        }

        private void drawSelector(GuiGraphics gui) {
            int u = isHoveredOrFocused() ? 115 : 0;
            // Keep both arrow caps intact; resize only the plain center panel.
            gui.blit(VALUE_FRAMES, getX(), getY(), u, 0, 8, 22, 230, 22);
            gui.blit(VALUE_FRAMES, getX() + 8, getY(), getWidth() - 16, 22, u + 8, 0, 99, 22, 230, 22);
            gui.blit(VALUE_FRAMES, getX() + getWidth() - 8, getY(), u + 107, 0, 8, 22, 230, 22);
        }

        private void drawAction(GuiGraphics gui) {
            ResourceLocation skin = CreateCompat.loaded() ? WIDGETS : SELECTOR;
            int u = CreateCompat.loaded() ? !active ? 90 : selected.get() ? 72 : isHoveredOrFocused() ? 18 : 0
                    : isHoveredOrFocused() ? 18 : 0;
            int v = CreateCompat.loaded() ? 0 : 22;
            int textureHeight = CreateCompat.loaded() ? 256 : 64;
            // Stretch only the blank center; action buttons never inherit selector arrows.
            gui.blit(skin, getX(), getY(), u, v, 3, 18, 256, textureHeight);
            gui.blit(skin, getX() + 3, getY(), getWidth() - 6, 18, u + 3, v, 12, 18, 256, textureHeight);
            gui.blit(skin, getX() + getWidth() - 3, getY(), u + 15, v, 3, 18, 256, textureHeight);
        }

        @Override
        public boolean mouseScrolled(double x, double y, double vertical) {
            if (!selector || !active || !visible || !clicked(x, y) || vertical == 0) return false;
            if (vertical < 0 && secondary != null) secondary.run();
            else onPress();
            return true;
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (secondary != null && active && visible && clicked(x, y)
                    && (button == 1 || button == 0 && selector && x < getX() + 10)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                secondary.run();
                return true;
            }
            return super.mouseClicked(x, y, button);
        }
    }
}
