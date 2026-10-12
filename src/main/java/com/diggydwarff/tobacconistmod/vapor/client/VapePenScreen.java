package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.vapor.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class VapePenScreen extends AbstractContainerScreen<VapePenMenu> {
    public VapePenScreen(VapePenMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageHeight = 194; inventoryLabelY = 100; }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("tobacconistmod.vapor.refill"), ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0)).bounds(leftPos + 19, topPos + 72, 64, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("tobacconistmod.vapor.charge"), ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1)).bounds(leftPos + 93, topPos + 72, 64, 18).build());
    }
    @Override protected void renderBg(GuiGraphics gui, float partial, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF302B25);
        gui.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xFFC3B79C);
        for (var slot : menu.slots) {
            gui.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF302B25);
            gui.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0xFF8E8777);
        }
    }
    @Override protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 7, 0x302B25, false);
        gui.drawString(font, Component.translatable("tobacconistmod.vapor.pen_status", VapePenItem.volume(menu.pen()), VaporData.charge(menu.pen())), 8, 23, 0x302B25, false);
        gui.drawString(font, Component.translatable("tobacconistmod.vapor.liquid_label"), 29, 35, 0x302B25, false);
        gui.drawString(font, Component.translatable("tobacconistmod.vapor.redstone_label"), 100, 35, 0x302B25, false);
        gui.drawString(font, playerInventoryTitle, 8, 100, 0x302B25, false);
    }
    @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partial) { renderBackground(gui, mouseX, mouseY, partial); super.render(gui, mouseX, mouseY, partial); renderTooltip(gui, mouseX, mouseY); }
}
