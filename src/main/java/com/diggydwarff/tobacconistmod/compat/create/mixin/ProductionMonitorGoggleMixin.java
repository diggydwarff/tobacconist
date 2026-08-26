package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Locale;

/** Adds live Production Monitor telemetry to Create's Goggle overlay when Create is installed. */
@Mixin(value = ProductionMonitorBlockEntity.class, remap = false)
public abstract class ProductionMonitorGoggleMixin implements IHaveGoggleInformation {
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        ProductionMonitorBlockEntity monitor = (ProductionMonitorBlockEntity) (Object) this;

        CreateLang.text("Production Monitor")
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);

        ItemStack filter = monitor.getFilter();
        Component filterLine = filter.isEmpty()
                ? Component.translatable("tobacconistmod.ui.monitor_filter_all")
                : Component.translatable("tobacconistmod.ui.monitor_filter", filter.getHoverName());
        CreateLang.builder().add(filterLine.copy().withStyle(ChatFormatting.GRAY)).forGoggles(tooltip, 1);

        Component countLine;
        Component rateLine;
        String rate = formatDecimal(monitor.getRollingRate());
        switch (monitor.getCountMode()) {
            case STACKS -> {
                countLine = Component.translatable(
                        "tobacconistmod.display.production_monitor_count.stacks",
                        formatDecimal(monitor.getCount() / 64.0D),
                        formatDecimal(monitor.getTarget() / 64.0D));
                rateLine = Component.translatable("tobacconistmod.display.production_monitor_rate.stacks", rate);
            }
            case TRANSFERS -> {
                countLine = Component.translatable(
                        "tobacconistmod.display.production_monitor_count.transfers",
                        formatWhole(monitor.getCount()), formatWhole(monitor.getTarget()));
                rateLine = Component.translatable("tobacconistmod.display.production_monitor_rate.transfers", rate);
            }
            default -> {
                countLine = Component.translatable(
                        "tobacconistmod.display.production_monitor_count.items",
                        formatWhole(monitor.getCount()), formatWhole(monitor.getTarget()));
                rateLine = Component.translatable("tobacconistmod.display.production_monitor_rate.items", rate);
            }
        }
        CreateLang.builder().add(countLine.copy().withStyle(ChatFormatting.GREEN)).forGoggles(tooltip, 1);
        CreateLang.builder().add(rateLine.copy().withStyle(ChatFormatting.GRAY)).forGoggles(tooltip, 1);

        Component status = !monitor.isTargetValid()
                ? Component.translatable("tobacconistmod.display.production_monitor_no_target")
                : monitor.getAtTargetMode() != ProductionMonitorBlockEntity.AtTargetMode.RESET_COUNT
                    && monitor.getCount() >= monitor.getTarget()
                ? Component.translatable("tobacconistmod.display.production_monitor_target_reached")
                : Component.translatable("tobacconistmod.display.production_monitor_counting");
        CreateLang.builder().add(status.copy().withStyle(monitor.isTargetValid()
                ? ChatFormatting.GREEN : ChatFormatting.YELLOW)).forGoggles(tooltip, 1);
        return true;
    }

    private static String formatWhole(long value) {
        if (value < 1_000L) return Long.toString(value);
        if (value < 1_000_000L) return compact(value, 1_000.0D, "k");
        if (value < 1_000_000_000L) return compact(value, 1_000_000.0D, "M");
        return compact(value, 1_000_000_000.0D, "B");
    }

    private static String formatDecimal(double value) {
        if (value >= 1_000.0D) return formatWhole(Math.round(value));
        if (Math.abs(value - Math.rint(value)) < 0.0001D) return Long.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String compact(long value, double divisor, String suffix) {
        double scaled = value / divisor;
        String number = scaled >= 100.0D
                ? Long.toString(Math.round(scaled))
                : String.format(Locale.ROOT, "%.1f", scaled).replace(".0", "");
        return number + suffix;
    }
}
