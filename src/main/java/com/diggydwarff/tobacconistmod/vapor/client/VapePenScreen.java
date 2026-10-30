package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
import com.diggydwarff.tobacconistmod.vapor.VapePenMenu;
import com.diggydwarff.tobacconistmod.vapor.VaporData;
import com.diggydwarff.tobacconistmod.backport.GuiGraphics;
import com.diggydwarff.tobacconistmod.backport.Tooltip;
import com.diggydwarff.tobacconistmod.backport.BackportContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class VapePenScreen extends BackportContainerScreen<VapePenMenu> {
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
        chargeButton.setTooltip(Tooltip.create(Component.translatable("tobacconistmod.vapor.device_charge_detail", 2000 / VapePenItem.maxCharge(menu.pen()))));
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
        gauge(gui, 95, VapePenItem.charge(menu.pen()), VapePenItem.maxCharge(menu.pen()), 0xFFBB7575);
    }

    private void gauge(GuiGraphics gui, int x, int amount, int capacity, int color) {
        gui.fill(leftPos + x, topPos + 75, leftPos + x + 64, topPos + 78, 0xFF302C29);
        gui.fill(leftPos + x, topPos + 75, leftPos + x + com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(amount, 0, capacity) * 64 / capacity, topPos + 78, color);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        String heading = font.plainSubstrByWidth(title.getString(), imageWidth - 16);
        gui.drawString(font, heading, (imageWidth - font.width(heading)) / 2, 4, VaporGui.TITLE, false);
        gui.drawString(font, text("liquid_label"), 26, 30, VaporGui.TEXT, false);
        gui.drawString(font, text("redstone_label"), 119, 30, VaporGui.TEXT, false);
        gui.drawString(font, VapePenItem.volume(menu.pen()) + "/" + VapePenItem.capacity(menu.pen()) + " mB", 18, 64, VaporGui.TEXT, false);
        gui.drawString(font, VapePenItem.chargePercent(menu.pen()) + "%", 95, 64, VaporGui.TEXT, false);
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
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, partial);
        renderTooltip(gui, mouseX, mouseY);
    }
}
