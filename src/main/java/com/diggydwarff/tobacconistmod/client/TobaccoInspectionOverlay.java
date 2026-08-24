package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.block.AbstractTallTobaccoCropBlock;
import com.diggydwarff.tobacconistmod.block.entity.BarrelEnvironmentHelper;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelMode;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.HangingTobaccoBlockEntity;
import com.diggydwarff.tobacconistmod.block.custom.HangingTobaccoBlock;
import com.diggydwarff.tobacconistmod.compat.SpectaclesEquipmentHelper;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoGrowthHelper;
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
    private static final int BACKGROUND = 0xD0101010;
    private static final int BORDER = 0xFF8A633A;
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

        // Capacity is basic rack information rather than a specialist inspection statistic.
        // Show it whenever the player is actually looking at the rack so filling it gives an
        // immediate 0/16, 1/16 ... 16/16 readout. Spectacles keep the richer cure diagnostics.
        if (targeted instanceof TobaccoDryingRackBlockEntity rack
                && !SpectaclesEquipmentHelper.isWearing(minecraft.player)) {
            drawPanel(graphics, minecraft.font, new Inspection(
                    "Tobacco Drying Rack",
                    List.of(new Line("Leaves: " + rack.getLeafCount() + "/16", TEXT))
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
            lines.add(new Line("Growth: " + pct + "%", age >= maxAge ? GOOD : TEXT));
            lines.add(new Line("Conditions: " + conditionName(environment), conditionColor(environment)));
            if (TobacconistConfig.isQualitySystemEnabled()) {
                int maxPotential = Math.min(70, potential + 10);
                lines.add(new Line("Potential quality: " + potential + "-" + maxPotential, MUTED));
            }
            if (age >= maxAge) {
                lines.add(new Line("Ready to harvest", GOOD));
            }
            return new Inspection(crop.getDisplayName(), lines);
        }

        if (state.getBlock() instanceof HangingTobaccoBlock) {
            BlockPos upperPos = state.getValue(HangingTobaccoBlock.HALF) == DoubleBlockHalf.UPPER
                    ? pos
                    : pos.above();
            BlockEntity hangingEntity = minecraft.level.getBlockEntity(upperPos);
            if (hangingEntity instanceof HangingTobaccoBlockEntity hanging) {
                List<Line> lines = new ArrayList<>();
                ItemStack stack = hanging.getStoredLeaf();
                lines.add(new Line("Leaves: " + hanging.getLeafCount() + "/16", TEXT));
                if (!stack.isEmpty()) {
                    lines.add(new Line(stack.getHoverName().getString(), TEXT));
                    lines.add(new Line("Method: " + hanging.getCurrentCureMethod(), hanging.isDryingActive() ? GOOD : WARN));
                    lines.add(new Line("Progress: " + hanging.getDryProgressPercent() + "%", TEXT));
                    if (hanging.isFinished()) {
                        lines.add(new Line("Finished", GOOD));
                    } else if (hanging.isDryingActive()) {
                        lines.add(new Line("Est. remaining: " + formatTicks(hanging.getEstimatedTicksRemaining()), MUTED));
                    } else {
                        lines.add(new Line("Processing paused", WARN));
                    }
                    addQualityLine(lines, stack);
                }
                return new Inspection("Hanging Tobacco Bunch", lines);
            }
        }

        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        if (blockEntity instanceof TobaccoDryingRackBlockEntity rack) {
            List<Line> lines = new ArrayList<>();
            lines.add(new Line("Leaves: " + rack.getLeafCount() + "/16", rack.hasLeaves() ? TEXT : MUTED));
            if (!rack.hasLeaves()) {
                return new Inspection("Tobacco Drying Rack", lines);
            }

            ItemStack stack = rack.getStoredLeaf();
            lines.add(new Line(stack.getHoverName().getString(), TEXT));
            lines.add(new Line("Method: " + rack.getCurrentCureMethod(), rack.isDryingActive() ? GOOD : WARN));
            lines.add(new Line("Progress: " + rack.getDryProgressPercent() + "%", TEXT));

            if (rack.isFinished()) {
                lines.add(new Line("Finished", GOOD));
            } else if (rack.isDryingActive()) {
                lines.add(new Line("Est. remaining: " + formatTicks(rack.getEstimatedTicksRemaining()), MUTED));
            } else {
                lines.add(new Line("Processing paused", WARN));
            }

            addQualityLine(lines, stack);
            return new Inspection("Tobacco Drying Rack", lines);
        }

        if (blockEntity instanceof TobaccoBarrelBlockEntity barrel) {
            List<Line> lines = new ArrayList<>();
            ItemStack stack = barrel.getStoredTobacco();
            if (stack.isEmpty()) {
                lines.add(new Line("Empty", MUTED));
                return new Inspection("Tobacco Barrel", lines);
            }

            lines.add(new Line(stack.getHoverName().getString() + " x" + stack.getCount(), TEXT));
            lines.add(new Line("Mode: " + barrel.getModeNameForInspection(), barrel.getMode() == TobaccoBarrelMode.IDLE ? MUTED : GOOD));

            if (barrel.getMode() != TobaccoBarrelMode.IDLE) {
                lines.add(new Line("Progress: " + barrel.getProcessProgressPercent() + "%", TEXT));
            }

            int agedDays = TobaccoBarrelBlockEntity.getAgedDays(stack);
            if (agedDays > 0) {
                lines.add(new Line("Age: " + formatAge(agedDays), MUTED));
            }

            if (TobaccoBarrelBlockEntity.isRuined(stack)) {
                lines.add(new Line("Ruined", BAD));
            } else if (TobaccoBarrelBlockEntity.isFermented(stack)) {
                lines.add(new Line("Fermented", GOOD));
            }

            int warmth = BarrelEnvironmentHelper.getWarmth(minecraft.level, pos);
            int humidity = BarrelEnvironmentHelper.getHumidity(minecraft.level, pos);
            lines.add(new Line("Environment: warmth " + warmth + " / humidity " + humidity, MUTED));
            addQualityLine(lines, stack);
            return new Inspection("Tobacco Barrel", lines);
        }

        return null;
    }

    private static void addQualityLine(List<Line> lines, ItemStack stack) {
        if (!TobacconistConfig.isQualitySystemEnabled() || stack.isEmpty()) return;

        CompoundTag tag = LegacyItemTags.getTag(stack);
        if (TobaccoCuringHelper.isRawTobaccoLeaf(stack) && tag != null && tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            int quality = Math.max(0, Math.min(70, tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY)));
            lines.add(new Line("Growth quality: " + quality, qualityColor(quality, 70)));
            return;
        }

        int quality = TobaccoCuringHelper.getQuality(stack);
        lines.add(new Line(
                "Quality: " + quality + " (" + TobaccoCuringHelper.getQualityTier(quality) + ")",
                qualityColor(quality, 100)
        ));
    }

    private static void drawPanel(GuiGraphics graphics, Font font, Inspection inspection) {
        int width = font.width(inspection.title());
        for (Line line : inspection.lines()) {
            width = Math.max(width, font.width(line.text()));
        }
        width += 12;

        int lineHeight = 10;
        int height = 8 + lineHeight * (inspection.lines().size() + 1);
        int x = graphics.guiWidth() / 2 + 14;
        int y = graphics.guiHeight() / 2 - height / 2;

        if (x + width > graphics.guiWidth() - 4) {
            x = graphics.guiWidth() / 2 - width - 14;
        }
        y = Math.max(4, Math.min(y, graphics.guiHeight() - height - 4));

        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        graphics.fill(x, y, x + width, y + 1, BORDER);
        graphics.fill(x, y + height - 1, x + width, y + height, BORDER);
        graphics.fill(x, y, x + 1, y + height, BORDER);
        graphics.fill(x + width - 1, y, x + width, y + height, BORDER);

        int textX = x + 6;
        int textY = y + 5;
        graphics.drawString(font, inspection.title(), textX, textY, TITLE, false);
        textY += lineHeight;

        for (Line line : inspection.lines()) {
            graphics.drawString(font, line.text(), textX, textY, line.color(), false);
            textY += lineHeight;
        }
    }

    private static String conditionName(int environmentScore) {
        if (environmentScore >= 18) return "Excellent";
        if (environmentScore >= 10) return "Good";
        if (environmentScore >= 0) return "Fair";
        return "Poor";
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

    private static String formatAge(int days) {
        if (days >= 365) {
            return (days / 365) + "y " + (days % 365) + "d";
        }
        return days + "d";
    }

    private record Inspection(String title, List<Line> lines) {}
    private record Line(String text, int color) {}
}