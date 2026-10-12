package com.diggydwarff.tobacconistmod.vapor.client;

import com.diggydwarff.tobacconistmod.vapor.*;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;

public final class LiquidStationScreen extends AbstractContainerScreen<LiquidStationMenu> {
    private VaporGui.Control saveButton, blendButton, clearButton;

    public LiquidStationScreen(LiquidStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = LiquidStationMenu.WIDTH; imageHeight = menu.inventoryY() + 108;
        inventoryLabelX = 36; inventoryLabelY = menu.inventoryY() + 6;
    }
    private Component text(String key) { return Component.translatable("tobacconistmod.vapor." + key); }
    private void click(int id) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }
    private LiquidBlend saved() { return menu.slots.get(LiquidStationEntity.CARD).getItem().get(VaporContent.BLEND.get()); }
    private int vg() { var saved = saved(); return saved == null ? menu.value(2) : saved.vegetablePercent(); }
    private int strength() { var saved = saved(); return saved == null ? menu.value(3) : saved.strength(); }
    private VaporGui.Control icon(int id, int x, int y, int u, int v, String name) {
        var button = new VaporGui.Control(leftPos + x, topPos + y, u, v, text(name), () -> click(id), () -> id == 3 && menu.value(4) == 1);
        button.setTooltip(Tooltip.create(text(name))); addRenderableWidget(button); return button;
    }
    @Override protected void init() {
        super.init(); if (!menu.blending()) return;
        var ratio = new VaporGui.Control(leftPos + 14, topPos + 100, 95, () -> Component.literal(vg() + " / " + (100 - vg())), () -> click(1), () -> click(0), true);
        ratio.setTooltip(Tooltip.create(text("ratio_control"))); addRenderableWidget(ratio);
        var nic = new VaporGui.Control(leftPos + 116, topPos + 100, 68, () -> text("strength." + strength()), () -> click(2), () -> click(7), true);
        nic.setTooltip(Tooltip.create(text("nicotine_control"))); addRenderableWidget(nic);
        icon(3, 210, 102, 0, 80, "auto_button"); saveButton = icon(4, 36, 135, 48, 160, "save_card");
        blendButton = icon(5, 160, 135, 112, 32, "blend_button"); clearButton = icon(6, 61, 135, 16, 0, "clear_flavors");
    }
    private LiquidStationEntity station() { return minecraft.level != null && minecraft.level.getBlockEntity(menu.pos()) instanceof LiquidStationEntity station ? station : null; }
    @Override protected void renderBg(GuiGraphics gui, float partial, int mouseX, int mouseY) {
        VaporGui.panel(gui, menu.blending() ? VaporGui.BENCH : VaporGui.LEGACY, leftPos, topPos, imageWidth, menu.blending() ? LiquidStationMenu.BLEND_PANEL_HEIGHT : LiquidStationMenu.EXTRACTION_PANEL_HEIGHT);
        VaporGui.inventory(gui, leftPos + 28, topPos + menu.inventoryY());
        if (!menu.blending()) return;
        for (int i = 0; i < 6; i++) {
            int sx = leftPos + LiquidStationMenu.BLEND_SLOTS[i][0], sy = topPos + LiquidStationMenu.BLEND_SLOTS[i][1];
            int bx = sx + 22, by = sy + 14;
            if (i < 3 && menu.value(6 + i) > 0) {
                gui.fill(bx, by, bx + 40, by + 3, 0xFF302C29);
                gui.fill(bx, by, bx + Math.clamp(menu.value(6 + i), 0, 2000) * 40 / 2000, by + 3, 0xFFB3C3A3);
            }
            if (i < 3 && !menu.slots.get(i).hasItem() && menu.value(6 + i) > 0) {
                var ingredient = i == 0 ? VaporContent.VEGETABLE_BASE : i == 1 ? VaporContent.CLEAR_BASE : VaporContent.NICOTINE_EXTRACT;
                gui.renderFakeItem(new ItemStack(ingredient.get()), sx, sy);
            }
            if (i >= 3 && !menu.slots.get(i).hasItem() && station() != null) {
                var blend = VaporData.blend(station().tanks[i].getFluid());
                if (blend != null) for (var flavor : BottledMolassesFlavors.values()) if (blend.flavors().containsKey(flavor.getFlavorPath())) {
                    ItemStack ghost = new ItemStack(flavor.getEssenceItem());
                    gui.renderFakeItem(ghost, sx, sy);
                    int doses = menu.value(6 + i) / VaporData.BOTTLE;
                    gui.renderItemDecorations(font, ghost, sx, sy, doses == 1 ? null : String.valueOf(doses));
                    break;
                }
            }
        }
    }
    private void label(GuiGraphics gui, String key, int x, int y, int width) { gui.drawString(font, font.plainSubstrByWidth(text(key).getString(), width), x, y, VaporGui.TEXT, false); }
    @Override protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        String heading = font.plainSubstrByWidth(title.getString(), imageWidth - 16);
        gui.drawString(font, heading, (imageWidth - font.width(heading)) / 2, 4, VaporGui.TITLE, false);
        gui.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, VaporGui.TITLE, false);
        if (menu.blending()) {
            String[] names = {"vegetable_label", "clear_label", "nicotine_label"};
            for (int i = 0; i < 3; i++) {
                int x = LiquidStationMenu.BLEND_SLOTS[i][0];
                label(gui, names[i], x, 22, 62);
                String amount = menu.value(6 + i) == 0 ? text("empty_label").getString() : String.valueOf(menu.value(6 + i));
                gui.drawString(font, font.plainSubstrByWidth(amount, 40), x + 22, 38, VaporGui.TEXT, false);
            }
            label(gui, "essences_label", 14, 56, 100);
            for (int i = 3; i < 6; i++) {
                int x = LiquidStationMenu.BLEND_SLOTS[i][0];
                var blend = station() == null ? null : VaporData.blend(station().tanks[i].getFluid());
                if (blend == null || blend.flavors().isEmpty()) {
                    gui.drawString(font, text("empty_label"), x + 22, 74, 0x9D958C, false);
                    continue;
                }
                String name = VaporData.flavorName(blend.flavors().keySet().iterator().next()).getString();
                gui.drawString(font, fit(name, 40), x + 22, 74, VaporGui.TEXT, false);
            }
            label(gui, "ratio_label", 14, 89, 95); label(gui, "nicotine_label", 116, 89, 68); label(gui, "auto_button", 205, 89, 26);
            label(gui, "card_label", 14, 124, 55); label(gui, "empty_bottles", 82, 124, 50); label(gui, "output_label", 137, 124, 44); label(gui, "glass_label", 190, 124, 40);
        } else {
            label(gui, "input_label", 14, 26, 43); label(gui, "reagent_label", 61, 26, 44); label(gui, "bottle_label", 108, 26, 48);
            label(gui, "output_label", 190, 26, 40); label(gui, "fuel_label", 61, 63, 44); label(gui, "glass_label", 190, 63, 40); label(gui, "extracting", 86, 82, 92);
        }
    }
    private String fit(String value, int width) {
        return font.width(value) <= width ? value : font.plainSubstrByWidth(value, width - font.width("...")) + "...";
    }
    @Override public void render(GuiGraphics gui, int mouseX, int mouseY, float partial) {
        if (menu.blending()) {
            ItemStack card = menu.slots.get(LiquidStationEntity.CARD).getItem();
            var station = station();
            saveButton.active = card.is(VaporContent.RECIPE_CARD.get());
            blendButton.active = menu.slots.get(6).hasItem() && !menu.slots.get(7).hasItem() && station != null
                    && station.hasIngredients(station.desiredBlend(menu.value(2), menu.value(3), card));
            clearButton.visible = menu.value(9) + menu.value(10) + menu.value(11) > 0
                    || saved() != null && !saved().flavors().isEmpty() || menu.value(4) != 0;
        }
        renderBackground(gui, mouseX, mouseY, partial); super.render(gui, mouseX, mouseY, partial); renderTooltip(gui, mouseX, mouseY);
        if (!menu.blending()) return;
        for (int i = 0; i < 6; i++) {
            int x = leftPos + LiquidStationMenu.BLEND_SLOTS[i][0], y = topPos + LiquidStationMenu.BLEND_SLOTS[i][1];
            if (mouseX < x || mouseX > x + 62 || mouseY < y || mouseY > y + 18 || menu.slots.get(i).hasItem()) continue;
            var lines = new ArrayList<Component>();
            if (i >= 3 && station() != null) {
                var blend = VaporData.blend(station().tanks[i].getFluid());
                if (blend != null) blend.flavors().keySet().forEach(flavor -> lines.add(VaporData.flavorName(flavor)));
                lines.add(Component.translatable("tobacconistmod.vapor.essence_doses", menu.value(6 + i) / VaporData.BOTTLE));
            } else lines.add(Component.literal(menu.value(6 + i) + " / 2000 mB"));
            gui.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }
}
