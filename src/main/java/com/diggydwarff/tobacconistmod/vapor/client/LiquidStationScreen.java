package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.vapor.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class LiquidStationScreen extends AbstractContainerScreen<LiquidStationMenu> {
    public LiquidStationScreen(LiquidStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 248; imageHeight = 292; inventoryLabelX = 44; inventoryLabelY = 198;
    }
    private Component text(String key) { return Component.translatable("tobacconistmod.vapor." + key); }
    private void button(int id, int x, int y, int width, Component label) {
        addRenderableWidget(Button.builder(label, ignored -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .bounds(leftPos + x, topPos + y, width, 18).build());
    }
    @Override protected void init() {
        super.init();
        if (!menu.blending()) return;
        button(0, 14, 150, 24, Component.literal("−")); button(1, 40, 150, 24, Component.literal("+"));
        button(2, 69, 150, 73, text("nicotine_button")); button(3, 147, 150, 87, text("auto_button"));
        button(4, 14, 174, 105, text("save_card")); button(5, 125, 174, 109, text("blend_button"));
        for (int i = 0; i < 3; i++) button(6 + i, 36 + i * 62, 80, 24, Component.literal("↩"));
    }
    @Override protected void renderBg(GuiGraphics gui, float partial, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF302B25);
        gui.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC3B79C);
        gui.fill(x + 7, y + 22, x + imageWidth - 7, y + 140, 0xFF524B3E);
        for (var slot : menu.slots) if (slot.isActive()) {
            gui.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xFF302B25);
            gui.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xFF8E8777);
        }
        if (menu.blending()) {
            for (int i = 0; i < 6; i++) {
                int bx = x + 17 + i % 3 * 62, by = y + (i < 3 ? 63 : 101);
                gui.fill(bx, by, bx + 40, by + 3, 0xFF302B25);
                gui.fill(bx, by, bx + Math.clamp(menu.value(6 + i), 0, 2000) * 40 / 2000, by + 3, 0xFFAEC58C);
            }
        } else {
            gui.fill(x + 17, y + 104, x + 177, y + 110, 0xFF302B25);
            gui.fill(x + 17, y + 104, x + 17 + Math.min(160, menu.value(0) * 160 / Math.max(1, menu.value(12))), y + 110, 0xFFBE9860);
        }
    }
    @Override protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 10, 8, 0x302B25, false);
        String[] names = menu.blending() ? new String[]{"vegetable_label", "clear_label", "nicotine_label", "flavor_1", "flavor_2", "flavor_3"}
                : new String[]{"input_label", "reagent_label", "bottle_label"};
        for (int i = 0; i < names.length; i++) gui.drawString(font, text(names[i]), 17 + i % 3 * 62, i < 3 ? 29 : 68, 0xE7DECC, false);
        if (menu.blending()) {
            gui.drawString(font, text("empty_bottles"), 17, 108, 0xE7DECC, false);
            gui.drawString(font, text("card_label"), 79, 108, 0xE7DECC, false);
            gui.drawString(font, Component.translatable("tobacconistmod.vapor.settings", menu.value(2), 100 - menu.value(2),
                    Component.translatable("tobacconistmod.vapor.strength." + menu.value(3)),
                    Component.translatable("tobacconistmod.vapor.auto." + menu.value(4))), 14, 138, 0x302B25, false);
        } else {
            gui.drawString(font, text("fuel_label"), 79, 68, 0xE7DECC, false);
            gui.drawString(font, text(menu.value(0) > 0 ? "extracting" : "extraction_hint"), 14, 150, 0x302B25, false);
        }
        gui.drawString(font, text("output_label"), 141, 108, 0xE7DECC, false);
        gui.drawString(font, text("returns_label"), 203, 108, 0xE7DECC, false);
        gui.drawString(font, playerInventoryTitle, 44, 198, 0x302B25, false);
    }
    @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partial) {
        renderBackground(gui, mouseX, mouseY, partial); super.render(gui, mouseX, mouseY, partial); renderTooltip(gui, mouseX, mouseY);
        if (menu.blending()) for (int i = 0; i < 6; i++) {
            int x = leftPos + 17 + i % 3 * 62, y = topPos + (i < 3 ? 63 : 101);
            if (mouseX >= x && mouseX <= x + 40 && mouseY >= y - 2 && mouseY <= y + 5) {
                var lines = new java.util.ArrayList<Component>();
                lines.add(Component.literal(menu.value(6 + i) + " / 2000 mB"));
                if (minecraft.level.getBlockEntity(menu.pos()) instanceof LiquidStationEntity station && i >= 3) {
                    var blend = VaporData.blend(station.tanks[i].getFluid());
                    if (blend != null) blend.flavors().keySet().forEach(flavor -> lines.add(VaporData.flavorName(flavor)));
                }
                gui.renderComponentTooltip(font, lines, mouseX, mouseY);
            }
        }
    }
}
