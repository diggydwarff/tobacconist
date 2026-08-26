package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.diggydwarff.tobacconistmod.network.ProductionMonitorConfigPayload;
import com.diggydwarff.tobacconistmod.network.ProductionMonitorResetPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

/** Create-inspired configuration screen using the supplied Production Monitor UI assets. */
public class ProductionMonitorScreen extends AbstractContainerScreen<ProductionMonitorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TobacconistMod.MODID, "textures/gui/production_monitor.png");

    private static final int FILTER_X = 18, FILTER_Y = 36, FILTER_W = 16, FILTER_H = 16;
    private static final int SELECTOR_X = 106, SELECTOR_W = 115, SELECTOR_H = 22;
    private static final int[] SELECTOR_Y = {66, 90, 114, 138};
    private static final int RESET_X = 173, RESET_Y = 164, RESET_W = 18, RESET_H = 18;
    private static final int CONFIRM_X = 202, CONFIRM_Y = 164, CONFIRM_W = 18, CONFIRM_H = 18;
    private static final int LABEL_COLOR = 0xD3D3D3;
    private static final int VALUE_COLOR = 0xFFFFFF;
    private static final int TITLE_COLOR = 0x592424;

    private EditBox targetField;
    private ItemStack pendingFilter;
    private int pendingCountMode;
    private int pendingAtTarget;
    private int pendingOutput;
    private boolean pendingExternalReset;
    private boolean submitted;

    public ProductionMonitorScreen(ProductionMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 235;
        imageHeight = 188;
        pendingFilter = menu.getInitialFilter();
        pendingCountMode = menu.getCountModeOrdinal();
        pendingAtTarget = menu.getAtTargetOrdinal();
        pendingOutput = menu.getOutputOrdinal();
        pendingExternalReset = menu.isExternalResetEnabled();
    }

    @Override
    protected void init() {
        super.init();
        targetField = new EditBox(font, leftPos + 176, topPos + 42, 42, 12,
                Component.translatable("gui.tobacconistmod.production_monitor.target"));
        targetField.setBordered(false);
        targetField.setMaxLength(10);
        targetField.setTextColor(VALUE_COLOR);
        targetField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        targetField.setValue(Integer.toString(displayTarget(menu.getTarget(), pendingCountMode)));
        addRenderableWidget(targetField);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // The selector frames are baked from the supplied widgets PNG; current values remain runtime text.
        if (!pendingFilter.isEmpty()) {
            guiGraphics.renderItem(pendingFilter, leftPos + FILTER_X, topPos + FILTER_Y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int titleX = imageWidth / 2 - font.width(title) / 2;
        guiGraphics.drawString(font, title, titleX, 4, TITLE_COLOR, false);

        guiGraphics.drawString(font, Component.translatable("gui.tobacconistmod.production_monitor.count"),
                46, 27, LABEL_COLOR, false);
        guiGraphics.drawString(font, fitCountForMode(menu.getCount(), menu.getTarget(), 84), 79, 27, VALUE_COLOR, false);

        guiGraphics.drawString(font, Component.translatable("gui.tobacconistmod.production_monitor.rate"),
                46, 44, LABEL_COLOR, false);
        if (menu.isTargetValid()) {
            String unitKey = rateUnitKey();
            Component rate = Component.translatable(unitKey, formatRate(menu.getRollingRate()));
            guiGraphics.drawString(font, trimToWidth(rate, 92), 74, 44, VALUE_COLOR, false);
        } else {
            guiGraphics.drawString(font,
                    trimToWidth(Component.translatable("gui.tobacconistmod.production_monitor.no_valid_target"), 92),
                    74, 44, LABEL_COLOR, false);
        }

        guiGraphics.drawString(font, Component.translatable("gui.tobacconistmod.production_monitor.target"),
                174, 24, LABEL_COLOR, false);

        drawRow(guiGraphics, 0, "gui.tobacconistmod.production_monitor.count_mode",
                optionText("count_mode", pendingCountMode));
        drawRow(guiGraphics, 1, "gui.tobacconistmod.production_monitor.at_target",
                optionText("at_target", pendingAtTarget));
        drawRow(guiGraphics, 2, "gui.tobacconistmod.production_monitor.output",
                optionText("output", pendingOutput));
        drawRow(guiGraphics, 3, "gui.tobacconistmod.production_monitor.external_reset",
                optionText("external_reset", pendingExternalReset ? 1 : 0));
    }

    private void drawRow(GuiGraphics guiGraphics, int row, String labelKey, Component value) {
        int labelY = 73 + row * 24;
        guiGraphics.drawString(font, trimToWidth(Component.translatable(labelKey), 85), 16, labelY,
                LABEL_COLOR, false);
        int valueX = SELECTOR_X + (SELECTOR_W - font.width(value)) / 2;
        guiGraphics.drawString(font, trimToWidth(value, SELECTOR_W - 18), valueX, 73 + row * 24,
                VALUE_COLOR, true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderProductionMonitorTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderProductionMonitorTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (inside(mouseX, mouseY, 46, 24, 117, 16)) {
            guiGraphics.renderTooltip(font, countTooltip(), mouseX, mouseY);
            return;
        }
        if (inside(mouseX, mouseY, 46, 41, 120, 16) && menu.isTargetValid()) {
            String unitKey = rateUnitKey();
            guiGraphics.renderTooltip(font, Component.translatable(unitKey, formatExactRate(menu.getRollingRate())),
                    mouseX, mouseY);
            return;
        }
        if (inside(mouseX, mouseY, FILTER_X, FILTER_Y, FILTER_W, FILTER_H) && !pendingFilter.isEmpty()) {
            guiGraphics.renderTooltip(font, pendingFilter, mouseX, mouseY);
            return;
        }
        if (inside(mouseX, mouseY, RESET_X, RESET_Y, RESET_W, RESET_H)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.tobacconistmod.production_monitor.reset"), mouseX, mouseY);
            return;
        }
        if (inside(mouseX, mouseY, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H)) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.tobacconistmod.production_monitor.confirm"), mouseX, mouseY);
            return;
        }
        for (int row = 0; row < SELECTOR_Y.length; row++) {
            if (inside(mouseX, mouseY, SELECTOR_X, SELECTOR_Y[row], SELECTOR_W, SELECTOR_H)) {
                Component value = switch (row) {
                    case 0 -> optionText("count_mode", pendingCountMode);
                    case 1 -> optionText("at_target", pendingAtTarget);
                    case 2 -> optionText("output", pendingOutput);
                    default -> optionText("external_reset", pendingExternalReset ? 1 : 0);
                };
                guiGraphics.renderTooltip(font, value, mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside(mouseX, mouseY, FILTER_X, FILTER_Y, FILTER_W, FILTER_H)) {
            if (button == 1) {
                pendingFilter = ItemStack.EMPTY;
            } else if (button == 0) {
                ItemStack carried = menu.getCarried();
                if (carried.isEmpty() && minecraft != null && minecraft.player != null) {
                    carried = minecraft.player.getMainHandItem();
                }
                if (!carried.isEmpty()) pendingFilter = carried.copyWithCount(1);
            }
            return true;
        }

        if (inside(mouseX, mouseY, RESET_X, RESET_Y, RESET_W, RESET_H)) {
            if (button == 0) PacketDistributor.sendToServer(new ProductionMonitorResetPayload(menu.getBlockPos()));
            return true;
        }
        if (inside(mouseX, mouseY, CONFIRM_X, CONFIRM_Y, CONFIRM_W, CONFIRM_H)) {
            if (button == 0) {
                submitConfiguration();
                submitted = true;
                if (minecraft != null && minecraft.player != null) minecraft.player.closeContainer();
            }
            return true;
        }

        for (int row = 0; row < SELECTOR_Y.length; row++) {
            if (inside(mouseX, mouseY, SELECTOR_X, SELECTOR_Y[row], SELECTOR_W, SELECTOR_H)) {
                cycleRow(row, button == 1 ? -1 : 1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (int row = 0; row < SELECTOR_Y.length; row++) {
            if (inside(mouseX, mouseY, SELECTOR_X, SELECTOR_Y[row], SELECTOR_W, SELECTOR_H)) {
                cycleRow(row, scrollY > 0 ? -1 : 1);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void cycleRow(int row, int direction) {
        switch (row) {
            case 0 -> cycleCountModeSelection(direction);
            case 1 -> {
                pendingAtTarget = wrap(pendingAtTarget + direction, 3);
                if (pendingAtTarget == ProductionMonitorBlockEntity.AtTargetMode.RESET_COUNT.ordinal()
                        && pendingOutput == ProductionMonitorBlockEntity.OutputMode.HOLD.ordinal()) {
                    pendingOutput = ProductionMonitorBlockEntity.OutputMode.PULSE.ordinal();
                }
            }
            case 2 -> {
                pendingOutput = wrap(pendingOutput + direction, 3);
                if (pendingAtTarget == ProductionMonitorBlockEntity.AtTargetMode.RESET_COUNT.ordinal()
                        && pendingOutput == ProductionMonitorBlockEntity.OutputMode.HOLD.ordinal()) {
                    pendingOutput = ProductionMonitorBlockEntity.OutputMode.PULSE.ordinal();
                }
            }
            case 3 -> pendingExternalReset = !pendingExternalReset;
        }
    }

    private void submitConfiguration() {
        int target = ProductionMonitorBlockEntity.DEFAULT_TARGET;
        try {
            String value = targetField == null ? "" : targetField.getValue();
            if (!value.isBlank()) target = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            target = ProductionMonitorBlockEntity.MAX_TARGET;
        }
        target = Math.max(ProductionMonitorBlockEntity.MIN_TARGET, target);
        if (ProductionMonitorBlockEntity.CountMode.byOrdinal(pendingCountMode)
                == ProductionMonitorBlockEntity.CountMode.STACKS) {
            long rawItems = (long) target * 64L;
            target = (int) Math.min(ProductionMonitorBlockEntity.MAX_TARGET, rawItems);
        }
        PacketDistributor.sendToServer(new ProductionMonitorConfigPayload(
                menu.getBlockPos(), target, pendingCountMode, pendingAtTarget, pendingOutput,
                pendingExternalReset, pendingFilter.copy()));
    }

    @Override
    public void onClose() {
        if (!submitted) submitConfiguration();
        super.onClose();
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w
                && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    private String rateUnitKey() {
        ProductionMonitorBlockEntity.CountMode mode = ProductionMonitorBlockEntity.CountMode.byOrdinal(pendingCountMode);
        return switch (mode) {
            case TRANSFERS -> "gui.tobacconistmod.production_monitor.rate_transfers";
            case STACKS -> "gui.tobacconistmod.production_monitor.rate_stacks";
            case ITEMS -> "gui.tobacconistmod.production_monitor.rate_items";
        };
    }

    private void cycleCountModeSelection(int direction) {
        int previous = pendingCountMode;
        int next = cycleCountMode(previous, direction);
        if (targetField != null) {
            int visibleTarget = ProductionMonitorBlockEntity.DEFAULT_TARGET;
            try {
                if (!targetField.getValue().isBlank()) visibleTarget = Integer.parseInt(targetField.getValue());
            } catch (NumberFormatException ignored) {
                visibleTarget = ProductionMonitorBlockEntity.DEFAULT_TARGET;
            }
            ProductionMonitorBlockEntity.CountMode previousMode = ProductionMonitorBlockEntity.CountMode.byOrdinal(previous);
            ProductionMonitorBlockEntity.CountMode nextMode = ProductionMonitorBlockEntity.CountMode.byOrdinal(next);
            if (previousMode == ProductionMonitorBlockEntity.CountMode.ITEMS
                    && nextMode == ProductionMonitorBlockEntity.CountMode.STACKS) {
                visibleTarget = Math.max(1, (int) Math.ceil(visibleTarget / 64.0D));
            } else if (previousMode == ProductionMonitorBlockEntity.CountMode.STACKS
                    && nextMode == ProductionMonitorBlockEntity.CountMode.ITEMS) {
                visibleTarget = (int) Math.min(Integer.MAX_VALUE, (long) visibleTarget * 64L);
            }
            targetField.setValue(Integer.toString(visibleTarget));
        }
        pendingCountMode = next;
    }

    private static int cycleCountMode(int current, int direction) {
        // Display order is Items -> Stacks -> Transfers, while enum ordinals remain save-compatible.
        int[] order = {
                ProductionMonitorBlockEntity.CountMode.ITEMS.ordinal(),
                ProductionMonitorBlockEntity.CountMode.STACKS.ordinal(),
                ProductionMonitorBlockEntity.CountMode.TRANSFERS.ordinal()
        };
        int index = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i] == current) {
                index = i;
                break;
            }
        }
        return order[wrap(index + direction, order.length)];
    }

    private Component optionText(String id, int value) {
        return Component.translatable("gui.tobacconistmod.production_monitor.option." + id + "." + value);
    }

    private Component trimToWidth(Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String raw = text.getString();
        String ellipsis = "…";
        while (!raw.isEmpty() && font.width(raw + ellipsis) > maxWidth) raw = raw.substring(0, raw.length() - 1);
        return Component.literal(raw + ellipsis);
    }

    private Component fitCountForMode(long count, int target, int maxWidth) {
        if (ProductionMonitorBlockEntity.CountMode.byOrdinal(pendingCountMode)
                == ProductionMonitorBlockEntity.CountMode.STACKS) {
            String countStacks = formatStackAmount(count / 64.0D);
            long targetStacks = Math.max(1L, (long) Math.ceil(target / 64.0D));
            String exact = countStacks + " / " + targetStacks;
            if (font.width(exact) <= maxWidth) return Component.literal(exact);
            return Component.literal(abbreviate(Math.round(count / 64.0D)) + " / " + abbreviate(targetStacks));
        }
        String exact = count + " / " + target;
        if (font.width(exact) <= maxWidth) return Component.literal(exact);
        return Component.literal(abbreviate(count) + " / " + abbreviate(target));
    }

    private Component countTooltip() {
        if (ProductionMonitorBlockEntity.CountMode.byOrdinal(pendingCountMode)
                == ProductionMonitorBlockEntity.CountMode.STACKS) {
            return Component.literal(formatStackAmount(menu.getCount() / 64.0D) + " / "
                    + Math.max(1L, (long) Math.ceil(menu.getTarget() / 64.0D)) + " stacks");
        }
        return Component.literal(menu.getCount() + " / " + menu.getTarget());
    }

    private static int displayTarget(int rawTarget, int countModeOrdinal) {
        if (ProductionMonitorBlockEntity.CountMode.byOrdinal(countModeOrdinal)
                != ProductionMonitorBlockEntity.CountMode.STACKS) {
            return rawTarget;
        }
        return Math.max(1, (int) Math.ceil(rawTarget / 64.0D));
    }

    private static String formatStackAmount(double stacks) {
        if (stacks >= 100.0D) return Long.toString(Math.round(stacks));
        if (Math.abs(stacks - Math.rint(stacks)) < 0.0001D) return Long.toString(Math.round(stacks));
        return String.format(Locale.ROOT, "%.1f", stacks);
    }

    private static String formatRate(double rate) {
        if (rate >= 1_000.0D) return abbreviate(Math.round(rate));
        return formatExactRate(rate);
    }

    private static String formatExactRate(double rate) {
        if (rate >= 100.0D) return Long.toString(Math.round(rate));
        return String.format(Locale.ROOT, "%.1f", rate);
    }

    private static String abbreviate(long value) {
        if (value < 1_000L) return Long.toString(value);
        if (value < 1_000_000L) return compact(value, 1_000D, "k");
        if (value < 1_000_000_000L) return compact(value, 1_000_000D, "M");
        return compact(value, 1_000_000_000D, "B");
    }

    private static String compact(long value, double divisor, String suffix) {
        double scaled = value / divisor;
        return scaled >= 100.0D ? Math.round(scaled) + suffix
                : String.format(Locale.ROOT, "%.1f%s", scaled, suffix).replace(".0" + suffix, suffix);
    }

    private static int wrap(int value, int size) {
        int wrapped = value % size;
        return wrapped < 0 ? wrapped + size : wrapped;
    }
}
