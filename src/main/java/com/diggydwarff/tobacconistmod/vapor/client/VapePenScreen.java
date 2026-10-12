package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
import com.diggydwarff.tobacconistmod.vapor.VapePenMenu;
import com.diggydwarff.tobacconistmod.vapor.VaporData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class VapePenScreen extends AbstractContainerScreen<VapePenMenu> {
    private VaporGui.Control refillButton;
    private VaporGui.Control chargeButton;

    public VapePenScreen(VapePenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.isBoxMod() ? 254 : 222;
        inventoryLabelX = 8;
        inventoryLabelY = menu.isBoxMod() ? 152 : 120;
    }

    private Component text(String key) {
        return Component.translatable("tobacconistmod.vapor." + key);
    }

    @Override
    protected void init() {
        super.init();
        refillButton = new VaporGui.Control(leftPos + 18, topPos + 86, 64, () -> text("refill"),
                () -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0), null);
        chargeButton = new VaporGui.Control(leftPos + 95, topPos + 86, 64, () -> text("charge"),
                () -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1), null);
        refillButton.setTooltip(Tooltip.create(text("refill_detail")));
        chargeButton.setTooltip(Tooltip.create(text("charge_detail")));
        addRenderableWidget(refillButton);
        addRenderableWidget(chargeButton);
        if (menu.isBoxMod()) {
            VaporGui.Control power = new VaporGui.Control(leftPos + 18, topPos + 114, 68,
                    () -> Component.translatable("tobacconistmod.vapor.wattage", VapePenItem.wattage(menu.pen())),
                    () -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2),
                    () -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 3), true);
            power.setTooltip(Tooltip.create(text("wattage_detail")));
            addRenderableWidget(power);
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partial, int mouseX, int mouseY) {
        boolean box = menu.isBoxMod();
        VaporGui.panel(gui, box ? VaporGui.BOX : VaporGui.PEN, leftPos, topPos, 176, box ? 144 : 112);
        VaporGui.inventory(gui, leftPos, topPos + (box ? 146 : 114));
        gauge(gui, 18, VapePenItem.volume(menu.pen()), VapePenItem.capacity(menu.pen()), 0xFFAAC6C4);
        gauge(gui, 95, VaporData.charge(menu.pen()), VaporData.MAX_CHARGE, 0xFFBB7575);
    }

    private void gauge(GuiGraphics gui, int x, int amount, int capacity, int color) {
        gui.fill(leftPos + x, topPos + 75, leftPos + x + 64, topPos + 78, 0xFF302C29);
        gui.fill(leftPos + x, topPos + 75, leftPos + x + amount * 64 / capacity, topPos + 78, color);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        String heading = font.plainSubstrByWidth(title.getString(), imageWidth - 16);
        gui.drawString(font, heading, (imageWidth - font.width(heading)) / 2, 4, VaporGui.TITLE, false);
        gui.drawString(font, text("liquid_label"), 26, 30, VaporGui.TEXT, false);
        gui.drawString(font, text("redstone_label"), 119, 30, VaporGui.TEXT, false);
        gui.drawString(font, VapePenItem.volume(menu.pen()) + "/" + VapePenItem.capacity(menu.pen()) + " mB", 18, 64, VaporGui.TEXT, false);
        gui.drawString(font, VaporData.charge(menu.pen()) + "%", 95, 64, VaporGui.TEXT, false);
        if (menu.isBoxMod()) {
            gui.drawString(font, text("puffs_label"), 95, 114, VaporGui.TEXT, false);
            gui.drawString(font, Integer.toString(VapePenItem.puffCount(menu.pen())), 95, 126, VaporGui.TEXT, false);
        }
        gui.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, VaporGui.TITLE, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partial) {
        refillButton.active = menu.canRefill();
        chargeButton.active = menu.canCharge();
        renderBackground(gui, mouseX, mouseY, partial);
        super.render(gui, mouseX, mouseY, partial);
        renderTooltip(gui, mouseX, mouseY);
    }
}
