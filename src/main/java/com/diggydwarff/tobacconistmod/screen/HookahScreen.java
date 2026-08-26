package com.diggydwarff.tobacconistmod.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * Runtime Hookah GUI.
 *
 * <p>Like Create's container screens, this renders independent texture
 * segments rather than stretching a complete mockup screenshot.</p>
 */
public class HookahScreen extends AbstractContainerScreen<HookahMenu> {
    private static final int INVENTORY_PANEL_X = 12;
    private static final int INVENTORY_PANEL_Y = 107;

    private static final int FUEL_BAR_X = 175;
    private static final int FUEL_BAR_Y = 28;

    public HookahScreen(HookahMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = HookahMenu.GUI_WIDTH;
        this.imageHeight = HookahMenu.GUI_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        HookahGuiTextures.HOOKAH.render(graphics, x, y);
        HookahGuiTextures.PLAYER_INVENTORY.render(
                graphics,
                x + INVENTORY_PANEL_X,
                y + INVENTORY_PANEL_Y
        );

        renderFuelBar(graphics, x, y);

        graphics.drawString(
                this.font,
                this.title,
                x + 8,
                y + 4,
                HookahGuiTextures.TITLE_COLOR,
                false
        );

        graphics.drawString(
                this.font,
                this.playerInventoryTitle,
                x + INVENTORY_PANEL_X + 8,
                y + INVENTORY_PANEL_Y + 6,
                HookahGuiTextures.INVENTORY_TITLE_COLOR,
                false
        );
    }

    /**
     * Create's Schematicannon fuel/progress bars crop a sprite according to the
     * current amount. Hookah does the same thing vertically, bottom-to-top.
     */
    private void renderFuelBar(GuiGraphics graphics, int x, int y) {
        HookahGuiTextures sprite = HookahGuiTextures.FUEL;
        int filled = this.menu.getFuelProgressScaled(sprite.height());
        if (filled <= 0) {
            return;
        }

        int yOffset = sprite.height() - filled;
        graphics.blit(
                sprite.location(),
                x + FUEL_BAR_X,
                y + FUEL_BAR_Y + yOffset,
                sprite.startX(),
                sprite.startY() + yOffset,
                sprite.width(),
                filled
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Labels are drawn alongside their separately rendered panels above.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        renderContextTooltips(graphics, mouseX, mouseY);
    }

    private void renderContextTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(
                FUEL_BAR_X - 2,
                FUEL_BAR_Y - 2,
                HookahGuiTextures.FUEL.width() + 4,
                HookahGuiTextures.FUEL.height() + 4,
                mouseX,
                mouseY
        )) {
            graphics.renderComponentTooltip(
                    this.font,
                    List.of(Component.translatable(
                            "container.tobacconistmod.hookah.fuel_remaining",
                            this.menu.getFuelPercent()
                    )),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (!this.menu.getSlot(HookahMenu.FUEL_MENU_SLOT_INDEX).hasItem()
                && isHovering(HookahMenu.FUEL_SLOT_X, HookahMenu.FUEL_SLOT_Y, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(
                    this.font,
                    List.of(Component.translatable("container.tobacconistmod.hookah.slot.fuel")),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (!this.menu.getSlot(HookahMenu.SHISHA_MENU_SLOT_INDEX).hasItem()
                && isHovering(HookahMenu.SHISHA_SLOT_X, HookahMenu.SHISHA_SLOT_Y, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(
                    this.font,
                    List.of(Component.translatable("container.tobacconistmod.hookah.slot.shisha")),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (!this.menu.getSlot(HookahMenu.WATER_MENU_SLOT_INDEX).hasItem()
                && isHovering(HookahMenu.WATER_SLOT_X, HookahMenu.WATER_SLOT_Y, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(
                    this.font,
                    List.of(Component.translatable("container.tobacconistmod.hookah.slot.water")),
                    mouseX,
                    mouseY
            );
        }
    }
}
