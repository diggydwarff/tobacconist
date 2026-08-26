package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.block.entity.BarrelEnvironmentHelper;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelMode;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.HangingTobaccoBlockEntity;
import com.diggydwarff.tobacconistmod.block.custom.HangingTobaccoBlock;
import com.diggydwarff.tobacconistmod.compat.SpectaclesEquipmentHelper;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoText;
import net.minecraft.network.chat.Component;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public final class TobaccoInspectionOverlay {
    private static final int BACKGROUND_TOP = 0xF0100010;
    private static final int BACKGROUND_BOTTOM = 0xF0100010;
    private static final int BORDER_LIGHT = 0x505000FF;
    private static final int BORDER_DARK = 0x5028007F;
    private static final int TITLE = 0xFFFFD27A;
    private static final int TEXT = 0xFFF0F0F0;
    private static final int MUTED = 0xFFB7B7B7;
    private static final int GOOD = 0xFF8FD694;
    private static final int WARN = 0xFFFFC66D;
    private static final int BAD = 0xFFFF7777;

    private TobaccoInspectionOverlay() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        if (minecraft.screen != null) return;

        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        BlockEntity targeted = minecraft.level.getBlockEntity(pos);

        // Rack capacity is visible without Spectacles; cure diagnostics still require Spectacles.
        if (targeted instanceof TobaccoDryingRackBlockEntity rack
                && !SpectaclesEquipmentHelper.isWearing(minecraft.player)) {
            drawPanel(graphics, minecraft.font, new Inspection(
                    Component.translatable(rack.getBlockState().getBlock().getDescriptionId()),
                    List.of(new Line(Component.translatable("tobacconistmod.ui.leaves", rack.getLeafCount(), rack.getMaxLeaves()), TEXT))
            ));
            return;
        }

        if (!SpectaclesEquipmentHelper.isWearing(minecraft.player)) return;

        Inspection inspection = inspect(minecraft, pos, state);
        if (inspection == null || inspection.lines().isEmpty()) return;

        drawPanel(graphics, minecraft.font, inspection);
    }

    private static Inspection inspect(Minecraft minecraft, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof AbstractTallTobaccoCropBlock crop) {
            BlockPos basePos = state.getValue(AbstractTallTobaccoCropBlock.HALF) == DoubleBlockHalf.UPPER
                    ? pos.below()
                    : pos;
            BlockState baseState = minecraft.level.getBlockState(basePos);
            int age = crop.getEffectiveAge(minecraft.level, basePos, baseState);
            int maxAge = crop.getMaxAge();
            int pct = Math.min(100, Math.round((age / (float) maxAge) * 100.0F));
            int potential = TobaccoGrowthHelper.calculateGrowthPotential(
                    minecraft.level, basePos, crop.getInspectionVariety(), age, maxAge
            );
            int environment = TobaccoGrowthHelper.calculateEnvironmentConditionScore(
                    minecraft.level, basePos, crop.getInspectionVariety()
            );

            List<Line> lines = new ArrayList<>();
            lines.add(new Line(Component.translatable("tobacconistmod.ui.growth", pct), age >= maxAge ? GOOD : TEXT));
            lines.add(new Line(Component.translatable("tobacconistmod.ui.conditions", TobaccoText.condition(environment)), conditionColor(environment)));
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int maxPotential = Math.min(70, potential + 10);
                lines.add(new Line(Component.translatable("tobacconistmod.ui.potential_quality", potential, maxPotential), MUTED));
            }
            if (age >= maxAge) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.ready_to_harvest"), GOOD));
            }
            return new Inspection(Component.translatable(state.getBlock().getDescriptionId()), lines);
        }

        if (state.getBlock() instanceof HangingTobaccoBlock) {
            BlockPos upperPos = state.getValue(HangingTobaccoBlock.HALF) == DoubleBlockHalf.UPPER
                    ? pos
                    : pos.above();
            BlockEntity hangingEntity = minecraft.level.getBlockEntity(upperPos);
            if (hangingEntity instanceof HangingTobaccoBlockEntity hanging) {
                List<Line> lines = new ArrayList<>();
                ItemStack stack = hanging.getStoredLeaf();
                lines.add(new Line(Component.translatable("tobacconistmod.ui.leaves", hanging.getLeafCount(), 16), TEXT));
                if (!stack.isEmpty()) {
                    lines.add(new Line(stack.getHoverName(), TEXT));
                    lines.add(new Line(Component.translatable("tobacconistmod.ui.method", hanging.getCurrentCureMethodComponent()), hanging.isDryingActive() ? GOOD : WARN));
                    lines.add(new Line(Component.translatable("tobacconistmod.ui.progress", hanging.getDryProgressPercent()), TEXT));
                    if (hanging.isFinished()) {
                        lines.add(new Line(Component.translatable("tobacconistmod.ui.finished"), GOOD));
                    } else if (hanging.isDryingActive()) {
                        lines.add(new Line(Component.translatable("tobacconistmod.ui.estimated_remaining", formatTicks(hanging.getEstimatedTicksRemaining())), MUTED));
                    } else {
                        lines.add(new Line(Component.translatable("tobacconistmod.ui.processing_paused"), WARN));
                    }
                    addQualityLine(lines, stack);
                }
                return new Inspection(Component.translatable("tobacconistmod.inspection.hanging_tobacco_bunch"), lines);
            }
        }

        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        if (blockEntity instanceof TobaccoDryingRackBlockEntity rack) {
            List<Line> lines = new ArrayList<>();
            lines.add(new Line(Component.translatable("tobacconistmod.ui.leaves", rack.getLeafCount(), rack.getMaxLeaves()), rack.hasLeaves() ? TEXT : MUTED));
            if (!rack.hasLeaves()) {
                return new Inspection(Component.translatable(rack.getBlockState().getBlock().getDescriptionId()), lines);
            }

            ItemStack stack = rack.getStoredLeaf();
            lines.add(new Line(stack.getHoverName(), TEXT));
            lines.add(new Line(Component.translatable("tobacconistmod.ui.method", rack.getCurrentCureMethodComponent()), rack.isDryingActive() ? GOOD : WARN));
            lines.add(new Line(Component.translatable("tobacconistmod.ui.progress", rack.getDryProgressPercent()), TEXT));

            if (rack.isFinished()) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.finished"), GOOD));
            } else if (rack.isDryingActive()) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.estimated_remaining", formatTicks(rack.getEstimatedTicksRemaining())), MUTED));
            } else {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.processing_paused"), WARN));
            }

            addQualityLine(lines, stack);
            return new Inspection(Component.translatable("block.tobacconistmod.tobacco_drying_rack_block"), lines);
        }

        CreateCompat.HomogenizationStatus homogenization = CreateCompat.getHomogenizationStatus(minecraft.level, pos);
        if (homogenization.relevant()) {
            List<Line> lines = new ArrayList<>();
            if (homogenization.finishMode()) {
                lines.add(new Line(Component.translatable(
                        "tobacconistmod.ui.homogenization_finish_batch",
                        homogenization.count()), TEXT));
            } else {
                lines.add(new Line(Component.translatable(
                        "tobacconistmod.ui.homogenization_batch",
                        homogenization.count(),
                        homogenization.target()), TEXT));
            }

            lines.add(new Line(Component.translatable(
                    "tobacconistmod.ui.current_average_quality",
                    String.format(java.util.Locale.ROOT, "%.1f", homogenization.averageQuality())), MUTED));
            lines.add(new Line(Component.translatable(
                    "tobacconistmod.ui.predicted_output_quality",
                    homogenization.predictedQuality()), TEXT));

            Component control;
            if (homogenization.signalStrength() == 0) {
                control = Component.translatable("tobacconistmod.homogenization.control.default");
            } else if (homogenization.signalStrength() == 15) {
                control = Component.translatable("tobacconistmod.homogenization.control.finish");
            } else {
                control = Component.translatable(
                        "tobacconistmod.homogenization.control.signal",
                        homogenization.signalStrength(),
                        homogenization.target());
            }
            lines.add(new Line(Component.translatable("tobacconistmod.ui.homogenization_control", control), MUTED));

            if (homogenization.incompatibleCount() > 0) {
                lines.add(new Line(Component.translatable(
                        "tobacconistmod.ui.homogenization_incompatible",
                        homogenization.incompatibleCount()), WARN));
            }

            Component status;
            int color;
            if (homogenization.processing()) {
                status = Component.translatable("tobacconistmod.homogenization.status.processing");
                color = GOOD;
            } else if (homogenization.finishMode() && !homogenization.finishArmed()) {
                status = Component.translatable("tobacconistmod.homogenization.status.rearm_finish");
                color = WARN;
            } else if (homogenization.uniform()) {
                status = Component.translatable("tobacconistmod.homogenization.status.uniform");
                color = MUTED;
            } else if (homogenization.ready()) {
                status = Component.translatable(homogenization.finishMode()
                        ? "tobacconistmod.homogenization.status.finish_ready"
                        : "tobacconistmod.homogenization.status.ready");
                color = GOOD;
            } else {
                status = Component.translatable("tobacconistmod.homogenization.status.filling");
                color = homogenization.signalStrength() == 0 ? MUTED : WARN;
            }
            lines.add(new Line(Component.translatable("tobacconistmod.ui.status", status), color));
            return new Inspection(Component.translatable("tobacconistmod.inspection.homogenization"), lines);
        }

        if (blockEntity instanceof TobaccoBarrelBlockEntity barrel) {
            List<Line> lines = new ArrayList<>();
            ItemStack stack = barrel.getStoredTobacco();
            if (stack.isEmpty()) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.empty"), MUTED));
                return new Inspection(Component.translatable("block.tobacconistmod.tobacco_barrel"), lines);
            }

            lines.add(new Line(Component.translatable("tobacconistmod.ui.item_count", stack.getHoverName(), stack.getCount()), TEXT));
            lines.add(new Line(Component.translatable("tobacconistmod.ui.mode", TobaccoText.barrelMode(barrel.getMode())), barrel.getMode() == TobaccoBarrelMode.IDLE ? MUTED : GOOD));

            if (barrel.getMode() != TobaccoBarrelMode.IDLE) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.progress", barrel.getProcessProgressPercent()), TEXT));
            }

            int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
            if (agedDays > 0) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.age", TobaccoText.ageDuration(agedDays)), MUTED));
            }

            if (TobaccoBarrelBlockEntity.isRuined(stack)) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.ruined"), BAD));
            } else if (TobaccoBarrelBlockEntity.isFermented(stack)) {
                lines.add(new Line(Component.translatable("tobacconistmod.ui.fermented"), GOOD));
            }

            int warmth = BarrelEnvironmentHelper.getWarmth(minecraft.level, pos);
            int humidity = BarrelEnvironmentHelper.getHumidity(minecraft.level, pos);
            lines.add(new Line(Component.translatable("tobacconistmod.ui.environment", warmth, humidity), MUTED));
            addQualityLine(lines, stack);
            return new Inspection(Component.translatable("block.tobacconistmod.tobacco_barrel"), lines);
        }

        return null;
    }

    private static void addQualityLine(List<Line> lines, ItemStack stack) {
        if (!TobacconistConfig.isQualitySystemEnabled() || stack.isEmpty()) return;

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack) && tag != null && tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            int quality = Math.max(0, Math.min(70, tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)));
            lines.add(new Line(Component.translatable("tobacconistmod.ui.growth_quality_value", quality), qualityColor(quality, 70)));
            return;
        }

        int quality = TobaccoCuringHelper.getQuality(stack);
        lines.add(new Line(
                Component.translatable("tobacconistmod.ui.quality", quality, TobaccoText.qualityTier(quality)),
                qualityColor(quality, 100)
        ));
    }

    private static void drawPanel(GuiGraphics graphics, Font font, Inspection inspection) {
        int iconSize = 14;
        int iconPadding = 3;
        int titleOffsetX = iconSize + iconPadding + 2;

        int width = font.width(inspection.title()) + titleOffsetX;
        for (Line line : inspection.lines()) {
            width = Math.max(width, font.width(line.text()));
        }
        width += 12;

        int lineHeight = 10;
        int headerHeight = 14;
        int height = 8 + headerHeight + lineHeight * inspection.lines().size();
        int x = graphics.guiWidth() / 2 + 14;
        int y = graphics.guiHeight() / 2 - height / 2;

        if (x + width > graphics.guiWidth() - 4) {
            x = graphics.guiWidth() / 2 - width - 14;
        }
        y = Math.max(4, Math.min(y, graphics.guiHeight() - height - 4));

        drawTooltipFrame(graphics, x, y, width, height);
        renderSpectaclesIcon(graphics, x + 3, y + 2);

        int textX = x + 6 + titleOffsetX;
        int textY = y + 5;
        graphics.drawString(font, inspection.title(), textX, textY, TITLE, false);
        textY = y + 5 + headerHeight;

        for (Line line : inspection.lines()) {
            graphics.drawString(font, line.text(), x + 6, textY, line.color(), false);
            textY += lineHeight;
        }
    }

    private static void drawTooltipFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fillGradient(x, y, x + width, y + height, BACKGROUND_TOP, BACKGROUND_BOTTOM);
        graphics.fill(x, y - 1, x + width, y, BORDER_LIGHT);
        graphics.fill(x, y + height, x + width, y + height + 1, BORDER_DARK);
        graphics.fill(x - 1, y, x, y + height, BORDER_LIGHT);
        graphics.fill(x + width, y, x + width + 1, y + height, BORDER_DARK);
        graphics.fillGradient(x, y, x + 1, y + height, BORDER_LIGHT, BORDER_DARK);
        graphics.fillGradient(x + width - 1, y, x + width, y + height, BORDER_LIGHT, BORDER_DARK);
        graphics.fill(x, y, x + width, y + 1, BORDER_LIGHT);
        graphics.fill(x, y + height - 1, x + width, y + height, BORDER_DARK);
    }

    private static void renderSpectaclesIcon(GuiGraphics graphics, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(1.0F, 1.0F, 1.0F);
        graphics.renderItem(new ItemStack(ModItems.TOBACCONISTS_SPECTACLES.get()), 0, 0);
        graphics.pose().popPose();
    }

    private static int conditionColor(int environmentScore) {
        if (environmentScore >= 18) return GOOD;
        if (environmentScore >= 10) return 0xFFC5E88A;
        if (environmentScore >= 0) return WARN;
        return BAD;
    }

    private static int qualityColor(int quality, int max) {
        float pct = max <= 0 ? 0.0F : quality / (float) max;
        if (pct >= 0.75F) return GOOD;
        if (pct >= 0.5F) return WARN;
        return BAD;
    }

    private static String formatTicks(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return minutes + ":" + String.format("%02d", remainder);
    }

    private record Inspection(Component title, List<Line> lines) {}
    private record Line(Component text, int color) {}
}