package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProcessingHelper;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Batch selection and execution for Create tobacco homogenization. */
public final class CreateTobaccoHomogenization {
    private static final Map<BatchKey, BatchPlan> ACTIVE_BATCHES = new HashMap<>();
    private static final Map<BasinKey, FinishRequest> FINISH_REQUESTS = new HashMap<>();
    private static final ThreadLocal<Integer> INTERNAL_EXTRACTION_DEPTH = ThreadLocal.withInitial(() -> 0);

    // Signal 0 is the no-redstone default. Signals 1-14 are intentionally simple,
    // reproducible factory lot sizes. Signal 15 is reserved for a one-shot finish command.
    private static final int[] SIGNAL_TARGETS = {
            64, 16, 32, 48, 64, 96, 128, 160,
            192, 256, 320, 384, 448, 512, 576
    };

    private CreateTobaccoHomogenization() {}

    public static boolean apply(BasinBlockEntity basin, CreateTobaccoHomogenizingRecipe recipe, boolean test) {
        if (basin == null || basin.getLevel() == null) return false;

        BasinKey basinKey = key(basin);
        BatchKey batchKey = batchKey(basin, recipe);
        if (test) {
            BatchPlan plan = selectBatch(basin, recipe);
            if (plan == null || !canExtractSnapshot(basin, plan.inputs())
                    || !acceptPlanOutputs(basin, plan.outputs(), true)) {
                ACTIVE_BATCHES.remove(batchKey);
                return false;
            }
            ACTIVE_BATCHES.put(batchKey, plan);
            return true;
        }

        BatchPlan plan = ACTIVE_BATCHES.remove(batchKey);
        removeOtherPlans(basinKey);
        if (plan == null) plan = selectBatch(basin, recipe);
        if (plan == null) return false;

        if (!containsSnapshot(basin.getInputInventory(), plan.inputs())
                || !canExtractSnapshot(basin, plan.inputs())) {
            if (plan.finishCommand()) FINISH_REQUESTS.remove(basinKey);
            return false;
        }
        if (!acceptPlanOutputs(basin, plan.outputs(), true)) return false;

        ExtractionResult extraction = extractSnapshot(basin, plan.inputs());
        if (!extraction.success()) {
            if (plan.finishCommand()) FINISH_REQUESTS.remove(basinKey);
            return false;
        }

        if (!acceptPlanOutputs(basin, copyOutputs(plan.outputs()), false)) {
            restoreExtraction(extraction);
            return false;
        }

        if (plan.finishCommand()) FINISH_REQUESTS.remove(basinKey);
        return true;
    }

    public static HomogenizationStatus getStatus(Level level, BlockPos pos) {
        if (level == null || pos == null) return HomogenizationStatus.NONE;

        BlockEntity inspected = level.getBlockEntity(pos);
        BasinBlockEntity basin;
        if (inspected instanceof BasinBlockEntity directBasin) {
            basin = directBasin;
        } else if (inspected instanceof MechanicalMixerBlockEntity
                && level.getBlockEntity(pos.below(2)) instanceof BasinBlockEntity mixerBasin) {
            basin = mixerBasin;
        } else {
            return HomogenizationStatus.NONE;
        }

        BasinKey basinKey = key(basin);
        BatchPlan active = findActivePlan(basinKey);
        if (active != null) {
            return new HomogenizationStatus(
                    true, active.batchSize(), active.target(), active.averageQuality(), active.outputQuality(),
                    true, active.signalStrength(), true, active.finishCommand(), active.finishCommand(),
                    active.incompatibleCount(), false
            );
        }

        FinishRequest finish = FINISH_REQUESTS.get(basinKey);
        if (finish != null) {
            QualityPreview preview = previewQuality(finish.inputs());
            return new HomogenizationStatus(
                    true, finish.count(), finish.count(), preview.averageQuality(), preview.outputQuality(),
                    !finish.uniform(), CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL,
                    false, true, true, finish.incompatibleCount(), finish.uniform()
            );
        }

        Group largest = findLargestCompatibleGroup(basin.getInputInventory(), ItemStack.EMPTY);
        if (largest == null || largest.count <= 0) return HomogenizationStatus.NONE;

        int signal = getControlSignal(basin);
        int incompatible = Math.max(0, countHomogenizable(basin.getInputInventory()) - largest.count);
        boolean uniform = !hasVisibleQualityVariance(largest.variants);

        if (signal == CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL) {
            QualityPreview preview = previewQuality(largest.variants);
            return new HomogenizationStatus(
                    true, largest.count, largest.count, preview.averageQuality(), preview.outputQuality(),
                    false, signal, false, true, false, incompatible, uniform
            );
        }

        int target = getBatchTarget(basin, signal);
        int considered = Math.min(largest.count, target);
        List<ExactStackCount> previewSelection = considered <= 0
                ? List.of()
                : selectProportional(largest, considered);
        QualityPreview preview = previewQuality(previewSelection);
        boolean ready = largest.count >= target && hasVisibleQualityVariance(previewSelection);

        return new HomogenizationStatus(
                true, considered, target, preview.averageQuality(), preview.outputQuality(),
                ready, signal, false, false, false, incompatible, uniform
        );
    }

    public static InventoryQualitySummary summarizeInventory(IItemHandler inventory) {
        if (inventory == null) return InventoryQualitySummary.EMPTY;

        ItemStack first = ItemStack.EMPTY;
        long qualityPoints = 0;
        int count = 0;
        boolean compatible = true;

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || !isHomogenizableLeaf(stack)) continue;

            if (first.isEmpty()) {
                first = stack.copyWithCount(1);
            } else if (!TobaccoProcessingHelper.areHomogenizingCompatibleLeaves(first, stack)) {
                compatible = false;
            }

            count += stack.getCount();
            qualityPoints += (long) TobaccoProcessingHelper.getHomogenizingQuality(stack) * stack.getCount();
        }

        if (count == 0) return InventoryQualitySummary.EMPTY;
        double average = qualityPoints / (double) count;
        return new InventoryQualitySummary(
                true, count, average, roundQuality(first, average), compatible, first
        );
    }

    /** Signals 1-14 are continuous targets; signal 15 is a one-shot finish command. */
    public static int getBatchTarget(BasinBlockEntity basin, int signalStrength) {
        if (basin == null) return 0;
        int signal = Math.max(0, Math.min(15, signalStrength));
        if (signal == CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL) return 0;
        return Math.min(SIGNAL_TARGETS[signal], getUsableCapacity(basin.getInputInventory()));
    }

    /** Called when the incoming analog signal changes. */
    public static void onControlSignalChanged(BasinBlockEntity basin, int oldSignal, int newSignal) {
        if (basin == null || basin.getLevel() == null || basin.getLevel().isClientSide) return;
        if (newSignal != CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL
                || oldSignal == CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL) {
            basin.notifyChangeOfContents();
            return;
        }

        BasinKey basinKey = key(basin);
        Group group = findLargestCompatibleGroup(basin.getInputInventory(), ItemStack.EMPTY);
        if (group == null || group.count < CreateTobaccoHomogenizingRecipe.MIN_BATCH_SIZE) {
            FINISH_REQUESTS.remove(basinKey);
            basin.notifyChangeOfContents();
            return;
        }

        int incompatible = Math.max(0, countHomogenizable(basin.getInputInventory()) - group.count);
        FINISH_REQUESTS.put(basinKey, new FinishRequest(
                List.copyOf(group.variants), group.count,
                !hasVisibleQualityVariance(group.variants), incompatible, group.template.copyWithCount(1)
        ));
        basin.notifyChangeOfContents();
        servicePendingCommands(basin);
    }

    /**
     * Runs one-shot uniform flushes and prevents a completely full, uniform Basin from
     * deadlocking an automated farm while waiting for a second quality that cannot enter.
     */
    public static void servicePendingCommands(BasinBlockEntity basin) {
        if (!isMixerInstalled(basin)) return;

        BasinKey basinKey = key(basin);
        FinishRequest finish = FINISH_REQUESTS.get(basinKey);
        if (finish != null && finish.uniform()) {
            if (flushSnapshotUnchanged(basin, finish.inputs())) {
                FINISH_REQUESTS.remove(basinKey);
                basin.notifyChangeOfContents();
            } else if (!containsSnapshot(basin.getInputInventory(), finish.inputs())) {
                FINISH_REQUESTS.remove(basinKey);
            }
            return;
        }

        if (finish != null) return;

        Group group = findLargestCompatibleGroup(basin.getInputInventory(), ItemStack.EMPTY);
        if (group == null || hasVisibleQualityVariance(group.variants)) return;
        if (group.count < getUsableCapacity(basin.getInputInventory())) return;

        // A physically full Basin cannot admit a later quality. Passing the already-uniform
        // tobacco through unchanged is the only non-destructive way to keep a continuous farm moving.
        flushSnapshotUnchanged(basin, List.copyOf(group.variants));
    }

    /** Avoids doing Tobacconist work every tick on unrelated Create Basins. */
    public static boolean shouldMonitor(BasinBlockEntity basin) {
        if (!isMixerInstalled(basin)) return false;
        BasinKey basinKey = key(basin);
        return FINISH_REQUESTS.containsKey(basinKey)
                || findActivePlan(basinKey) != null
                || countHomogenizable(basin.getInputInventory()) > 0;
    }

    public static boolean hasActiveBatch(BasinBlockEntity basin) {
        return basin != null && basin.getLevel() != null && findActivePlan(key(basin)) != null;
    }

    private static BatchPlan selectBatch(BasinBlockEntity basin, CreateTobaccoHomogenizingRecipe recipe) {
        ItemStack target = recipe.getLeafTemplate();
        if (target.isEmpty()) return null;

        BasinKey basinKey = key(basin);
        FinishRequest finish = FINISH_REQUESTS.get(basinKey);
        if (finish != null) {
            if (finish.uniform() || finish.template().getItem() != target.getItem()) return null;
            if (!containsSnapshot(basin.getInputInventory(), finish.inputs())) {
                FINISH_REQUESTS.remove(basinKey);
                return null;
            }
            return buildPlan(
                    finish.inputs(), finish.count(), finish.count(),
                    CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL,
                    true, finish.incompatibleCount(), finish.template()
            );
        }

        int signal = getControlSignal(basin);
        if (signal == CreateTobaccoHomogenizingRecipe.FINISH_SIGNAL) return null;

        Group group = findLargestCompatibleGroup(basin.getInputInventory(), target);
        if (group == null) return null;

        int batchSize = getBatchTarget(basin, signal);
        if (batchSize <= 0 || group.count < batchSize) return null;

        List<ExactStackCount> selected = selectProportional(group, batchSize);
        if (selected.isEmpty() || !hasVisibleQualityVariance(selected)) return null;

        int incompatible = Math.max(0, countHomogenizable(basin.getInputInventory()) - group.count);
        return buildPlan(selected, batchSize, batchSize, signal, false, incompatible, group.template);
    }

    private static BatchPlan buildPlan(List<ExactStackCount> selected, int batchSize, int target,
                                       int signalStrength, boolean finishCommand,
                                       int incompatibleCount, ItemStack template) {
        long qualityPoints = 0;
        int selectedCount = 0;
        for (ExactStackCount variant : selected) {
            qualityPoints += (long) TobaccoProcessingHelper.getHomogenizingQuality(variant.stack()) * variant.count();
            selectedCount += variant.count();
        }
        if (selectedCount != batchSize || batchSize <= 0) return null;

        double average = qualityPoints / (double) batchSize;
        int outputQuality = roundQuality(template, average);
        ItemStack outputTemplate = TobaccoProcessingHelper.buildHomogenizedLeafBatch(
                template, outputQuality, 1);
        if (outputTemplate.isEmpty()) return null;

        return new BatchPlan(
                List.copyOf(selected), splitOutput(outputTemplate, batchSize),
                batchSize, target, signalStrength, average, outputQuality,
                finishCommand, incompatibleCount
        );
    }

    private static Group findLargestCompatibleGroup(IItemHandler inventory, ItemStack target) {
        if (inventory == null) return null;
        ArrayList<Group> groups = new ArrayList<>();

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!target.isEmpty() && stack.getItem() != target.getItem()) continue;
            if (!isHomogenizableLeaf(stack)) continue;

            Group matched = null;
            for (Group group : groups) {
                if (TobaccoProcessingHelper.areHomogenizingCompatibleLeaves(group.template, stack)) {
                    matched = group;
                    break;
                }
            }
            if (matched == null) {
                matched = new Group(stack.copyWithCount(1));
                groups.add(matched);
            }
            matched.add(stack);
        }

        Group largest = null;
        for (Group group : groups) {
            if (largest == null || group.count > largest.count) largest = group;
        }
        return largest;
    }

    private static int countHomogenizable(IItemHandler inventory) {
        if (inventory == null) return 0;
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (isHomogenizableLeaf(stack)) count += stack.getCount();
        }
        return count;
    }

    private static boolean isHomogenizableLeaf(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && (TobaccoCuringHelper.isRawTobaccoLeaf(stack)
                || TobaccoCuringHelper.isDryTobaccoLeaf(stack));
    }

    private static boolean isMixerInstalled(BasinBlockEntity basin) {
        if (basin == null || basin.getLevel() == null) return false;
        BlockEntity operator = basin.getLevel().getBlockEntity(basin.getBlockPos().above(2));
        return operator instanceof MechanicalMixerBlockEntity;
    }

    /**
     * Reads homogenizer control from either half of the Create setup. A signal may be
     * applied to the Basin or directly to the Mechanical Mixer; when both are powered,
     * the stronger signal wins.
     */
    public static int getControlSignal(BasinBlockEntity basin) {
        if (basin == null || basin.getLevel() == null) return 0;

        Level level = basin.getLevel();
        int basinSignal = level.getBestNeighborSignal(basin.getBlockPos());
        int mixerSignal = 0;

        BlockPos mixerPos = basin.getBlockPos().above(2);
        if (level.getBlockEntity(mixerPos) instanceof MechanicalMixerBlockEntity) {
            mixerSignal = level.getBestNeighborSignal(mixerPos);
        }

        return Math.max(basinSignal, mixerSignal);
    }

    private static int getUsableCapacity(SmartInventory inventory) {
        int capacity = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) capacity += inventory.getSlotLimit(slot);
        return capacity;
    }

    /**
     * Count-proportional batch selection. If rounding would choose only one visible quality
     * even though the Basin contains variation, swap one item from a minority quality into
     * the batch. That guarantees each Mixer cycle actually reduces variation while still
     * keeping the selection as close to proportional as possible.
     */
    private static List<ExactStackCount> selectProportional(Group group, int targetCount) {
        if (group == null || targetCount <= 0 || group.count < targetCount || group.variants.isEmpty()) {
            return List.of();
        }
        if (targetCount == group.count) return List.copyOf(group.variants);

        int size = group.variants.size();
        int[] amounts = new int[size];
        long[] remainders = new long[size];
        int allocated = 0;

        for (int i = 0; i < size; i++) {
            ExactStackCount variant = group.variants.get(i);
            long scaled = (long) variant.count() * targetCount;
            amounts[i] = (int) (scaled / group.count);
            remainders[i] = scaled % group.count;
            allocated += amounts[i];
        }

        while (allocated < targetCount) {
            int best = -1;
            for (int i = 0; i < size; i++) {
                if (amounts[i] >= group.variants.get(i).count()) continue;
                if (best == -1 || remainders[i] > remainders[best]) best = i;
            }
            if (best == -1) return List.of();
            amounts[best]++;
            remainders[best] = -1;
            allocated++;
        }

        if (hasVisibleQualityVariance(group.variants) && !hasVisibleQualityVariance(group.variants, amounts)) {
            int selectedQuality = -1;
            int donor = -1;
            int donorAmount = 0;
            for (int i = 0; i < size; i++) {
                if (amounts[i] <= 0) continue;
                selectedQuality = TobaccoProcessingHelper.getHomogenizingQuality(group.variants.get(i).stack());
                if (amounts[i] > donorAmount) {
                    donor = i;
                    donorAmount = amounts[i];
                }
            }

            int minority = -1;
            int minorityCount = -1;
            for (int i = 0; i < size; i++) {
                if (TobaccoProcessingHelper.getHomogenizingQuality(group.variants.get(i).stack()) == selectedQuality) {
                    continue;
                }
                if (group.variants.get(i).count() > minorityCount) {
                    minority = i;
                    minorityCount = group.variants.get(i).count();
                }
            }

            if (donor >= 0 && minority >= 0 && amounts[donor] > 0
                    && amounts[minority] < group.variants.get(minority).count()) {
                amounts[donor]--;
                amounts[minority]++;
            }
        }

        ArrayList<ExactStackCount> selected = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (amounts[i] > 0) {
                selected.add(new ExactStackCount(group.variants.get(i).stack().copyWithCount(1), amounts[i]));
            }
        }
        return List.copyOf(selected);
    }

    private static boolean hasVisibleQualityVariance(List<ExactStackCount> variants) {
        int quality = Integer.MIN_VALUE;
        for (ExactStackCount variant : variants) {
            if (variant.count() <= 0) continue;
            int current = TobaccoProcessingHelper.getHomogenizingQuality(variant.stack());
            if (quality == Integer.MIN_VALUE) quality = current;
            else if (current != quality) return true;
        }
        return false;
    }

    private static boolean hasVisibleQualityVariance(List<ExactStackCount> variants, int[] amounts) {
        int quality = Integer.MIN_VALUE;
        for (int i = 0; i < variants.size(); i++) {
            if (amounts[i] <= 0) continue;
            int current = TobaccoProcessingHelper.getHomogenizingQuality(variants.get(i).stack());
            if (quality == Integer.MIN_VALUE) quality = current;
            else if (current != quality) return true;
        }
        return false;
    }

    private static QualityPreview previewQuality(Group group, int count) {
        if (group == null || count <= 0) return new QualityPreview(0.0D, 0);
        return previewQuality(selectProportional(group, Math.min(count, group.count)));
    }

    private static QualityPreview previewQuality(List<ExactStackCount> selectedVariants) {
        if (selectedVariants == null || selectedVariants.isEmpty()) return new QualityPreview(0.0D, 0);
        long qualityPoints = 0;
        int selected = 0;
        ItemStack template = ItemStack.EMPTY;
        for (ExactStackCount variant : selectedVariants) {
            if (template.isEmpty()) template = variant.stack();
            qualityPoints += (long) TobaccoProcessingHelper.getHomogenizingQuality(variant.stack()) * variant.count();
            selected += variant.count();
        }
        if (selected == 0) return new QualityPreview(0.0D, 0);
        double average = qualityPoints / (double) selected;
        return new QualityPreview(average, roundQuality(template, average));
    }

    private static int roundQuality(ItemStack template, double average) {
        int rounded = (int) Math.round(average);
        if (TobaccoCuringHelper.isRawTobaccoLeaf(template)) {
            return Math.max(0, Math.min(70, rounded));
        }
        return TobaccoCuringHelper.clampQuality(rounded);
    }

    private static boolean containsSnapshot(IItemHandler inventory, List<ExactStackCount> inputs) {
        for (ExactStackCount required : inputs) {
            if (countExact(inventory, required.stack()) < required.count()) return false;
        }
        return true;
    }

    private static boolean canExtractSnapshot(BasinBlockEntity basin, List<ExactStackCount> inputs) {
        beginInternalExtraction();
        try {
            for (ExactStackCount required : inputs) {
                if (simulateExtractExact(basin.getInputInventory(), required.stack(), required.count())
                        < required.count()) return false;
            }
            return true;
        } finally {
            endInternalExtraction();
        }
    }

    private static ExtractionResult extractSnapshot(BasinBlockEntity basin, List<ExactStackCount> inputs) {
        beginInternalExtraction();
        try {
            ArrayList<ExtractedStack> extracted = new ArrayList<>();
            for (ExactStackCount required : inputs) {
                int extractedCount = extractExact(
                        basin.getInputInventory(), required.stack(), required.count(), extracted);
                if (extractedCount < required.count()) {
                    ExtractionResult failed = new ExtractionResult(false, List.copyOf(extracted));
                    restoreExtraction(failed);
                    return ExtractionResult.FAILURE;
                }
            }
            return new ExtractionResult(true, List.copyOf(extracted));
        } finally {
            endInternalExtraction();
        }
    }

    private static boolean flushSnapshotUnchanged(BasinBlockEntity basin, List<ExactStackCount> inputs) {
        if (inputs == null || inputs.isEmpty() || !containsSnapshot(basin.getInputInventory(), inputs)) return false;

        List<ItemStack> outputs = splitUnchangedOutputs(inputs);
        if (!acceptPlanOutputs(basin, outputs, true) || !canExtractSnapshot(basin, inputs)) return false;

        ExtractionResult extraction = extractSnapshot(basin, inputs);
        if (!extraction.success()) return false;
        if (!acceptPlanOutputs(basin, copyOutputs(outputs), false)) {
            restoreExtraction(extraction);
            return false;
        }
        return true;
    }

    private static int countExact(IItemHandler inventory, ItemStack template) {
        if (inventory == null) return 0;
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (ItemStack.isSameItemSameTags(stack, template)) count += stack.getCount();
        }
        return count;
    }

    private static int simulateExtractExact(IItemHandler inventory, ItemStack template, int requested) {
        if (inventory == null || requested <= 0) return 0;
        int extracted = 0;
        for (int slot = 0; slot < inventory.getSlots() && extracted < requested; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!ItemStack.isSameItemSameTags(stack, template)) continue;
            ItemStack simulated = inventory.extractItem(slot, requested - extracted, true);
            if (ItemStack.isSameItemSameTags(simulated, template)) extracted += simulated.getCount();
        }
        return extracted;
    }

    private static int extractExact(IItemHandler inventory, ItemStack template, int requested,
                                    List<ExtractedStack> extracted) {
        if (inventory == null || requested <= 0) return 0;
        int total = 0;
        for (int slot = 0; slot < inventory.getSlots() && total < requested; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!ItemStack.isSameItemSameTags(stack, template)) continue;
            ItemStack out = inventory.extractItem(slot, requested - total, false);
            if (out.isEmpty() || !ItemStack.isSameItemSameTags(out, template)) continue;
            total += out.getCount();
            extracted.add(new ExtractedStack(inventory, out.copy()));
        }
        return total;
    }

    private static void restoreExtraction(ExtractionResult result) {
        if (result == null || result.extracted().isEmpty()) return;
        for (int i = result.extracted().size() - 1; i >= 0; i--) {
            ExtractedStack extracted = result.extracted().get(i);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(
                    extracted.inventory(), extracted.stack().copy(), false);
            for (int slot = 0; slot < extracted.inventory().getSlots() && !remainder.isEmpty(); slot++) {
                remainder = extracted.inventory().insertItem(slot, remainder, false);
            }
        }
    }

    private static boolean acceptPlanOutputs(BasinBlockEntity basin, List<ItemStack> outputs, boolean simulate) {
        if (basin == null || outputs == null || outputs.isEmpty()) return false;

        Direction outputDirection = basin.getBlockState().getValue(BasinBlock.FACING);
        if (outputDirection != Direction.DOWN) {
            return basin.acceptOutputs(outputs, List.of(), simulate);
        }

        SmartInventory inventory = basin.getOutputInventory();
        ArrayList<ItemStack> working = new ArrayList<>(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            working.add(inventory.getStackInSlot(slot).copy());
        }

        for (ItemStack output : outputs) {
            ItemStack remaining = output.copy();
            for (int slot = 0; slot < working.size() && !remaining.isEmpty(); slot++) {
                ItemStack existing = working.get(slot);
                if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, remaining)) continue;
                int limit = Math.min(inventory.getSlotLimit(slot), existing.getMaxStackSize());
                int moved = Math.min(remaining.getCount(), Math.max(0, limit - existing.getCount()));
                if (moved <= 0) continue;
                working.set(slot, existing.copyWithCount(existing.getCount() + moved));
                remaining.shrink(moved);
            }
            for (int slot = 0; slot < working.size() && !remaining.isEmpty(); slot++) {
                if (!working.get(slot).isEmpty()) continue;
                int limit = Math.min(inventory.getSlotLimit(slot), remaining.getMaxStackSize());
                int moved = Math.min(remaining.getCount(), limit);
                if (moved <= 0) continue;
                working.set(slot, remaining.copyWithCount(moved));
                remaining.shrink(moved);
            }
            if (!remaining.isEmpty()) return false;
        }

        if (!simulate) {
            for (int slot = 0; slot < working.size(); slot++) {
                inventory.setStackInSlot(slot, working.get(slot));
            }
        }
        return true;
    }

    private static List<ItemStack> splitOutput(ItemStack template, int count) {
        ArrayList<ItemStack> outputs = new ArrayList<>();
        int remaining = count;
        while (remaining > 0) {
            int amount = Math.min(remaining, template.getMaxStackSize());
            outputs.add(template.copyWithCount(amount));
            remaining -= amount;
        }
        return List.copyOf(outputs);
    }

    private static List<ItemStack> splitUnchangedOutputs(List<ExactStackCount> inputs) {
        ArrayList<ItemStack> outputs = new ArrayList<>();
        for (ExactStackCount input : inputs) {
            int remaining = input.count();
            while (remaining > 0) {
                int amount = Math.min(remaining, input.stack().getMaxStackSize());
                outputs.add(input.stack().copyWithCount(amount));
                remaining -= amount;
            }
        }
        return List.copyOf(outputs);
    }

    private static List<ItemStack> copyOutputs(List<ItemStack> outputs) {
        ArrayList<ItemStack> copies = new ArrayList<>(outputs.size());
        for (ItemStack output : outputs) copies.add(output.copy());
        return copies;
    }

    /** Generic extraction must not steal pending homogenizer input before the Mixer claims it. */
    public static boolean shouldBlockExternalInputExtraction(BasinBlockEntity basin, int slot) {
        if (basin == null || basin.getLevel() == null || isInternalExtraction()) return false;
        if (slot < 0 || slot >= basin.getInputInventory().getSlots()) return false;
        return isMixerInstalled(basin)
                && isHomogenizableLeaf(basin.getInputInventory().getStackInSlot(slot));
    }

    /**
     * Create normally permits only one stack with identical components in a Basin. Homogenizers
     * need repeated same-quality leaf stacks to occupy multiple slots, otherwise a long Q50 run
     * can block a later Q44 stack from ever reaching the machine.
     */
    public static boolean shouldAllowDuplicateInputStacks(BasinBlockEntity basin,
                                                           ItemStack first, ItemStack second) {
        return isMixerInstalled(basin) && isHomogenizableLeaf(first) && isHomogenizableLeaf(second);
    }

    public static boolean isInternalExtraction() {
        return INTERNAL_EXTRACTION_DEPTH.get() > 0;
    }

    private static void beginInternalExtraction() {
        INTERNAL_EXTRACTION_DEPTH.set(INTERNAL_EXTRACTION_DEPTH.get() + 1);
    }

    private static void endInternalExtraction() {
        int depth = INTERNAL_EXTRACTION_DEPTH.get() - 1;
        if (depth <= 0) INTERNAL_EXTRACTION_DEPTH.remove();
        else INTERNAL_EXTRACTION_DEPTH.set(depth);
    }

    public static void clear(BasinBlockEntity basin) {
        if (basin == null || basin.getLevel() == null) return;
        BasinKey basinKey = key(basin);
        removeOtherPlans(basinKey);
        FINISH_REQUESTS.remove(basinKey);
    }

    public static void clearIfOperatorInactive(BasinBlockEntity basin) {
        if (basin == null || basin.getLevel() == null) return;
        BasinKey basinKey = key(basin);
        if (findActivePlan(basinKey) == null) return;
        if (!isMixerInstalled(basin)) {
            removeOtherPlans(basinKey);
            return;
        }
        BlockEntity operator = basin.getLevel().getBlockEntity(basin.getBlockPos().above(2));
        if (operator instanceof MechanicalMixerBlockEntity mixer && !mixer.running) {
            removeOtherPlans(basinKey);
        }
    }

    private static BatchPlan findActivePlan(BasinKey basinKey) {
        for (Map.Entry<BatchKey, BatchPlan> entry : ACTIVE_BATCHES.entrySet()) {
            if (entry.getKey().basin().equals(basinKey)) return entry.getValue();
        }
        return null;
    }

    private static void removeOtherPlans(BasinKey basinKey) {
        ACTIVE_BATCHES.keySet().removeIf(key -> key.basin().equals(basinKey));
    }

    private static BasinKey key(BasinBlockEntity basin) {
        return new BasinKey(basin.getLevel().dimension(), basin.getBlockPos().immutable());
    }

    private static BatchKey batchKey(BasinBlockEntity basin, CreateTobaccoHomogenizingRecipe recipe) {
        return new BatchKey(key(basin), recipe.getLeafTemplate().getItem());
    }

    private static final class Group {
        private final ItemStack template;
        private final List<ExactStackCount> variants = new ArrayList<>();
        private int count;

        private Group(ItemStack template) {
            this.template = template;
        }

        private void add(ItemStack stack) {
            count += stack.getCount();
            for (int i = 0; i < variants.size(); i++) {
                ExactStackCount existing = variants.get(i);
                if (ItemStack.isSameItemSameTags(existing.stack(), stack)) {
                    variants.set(i, new ExactStackCount(existing.stack(), existing.count() + stack.getCount()));
                    return;
                }
            }
            variants.add(new ExactStackCount(stack.copyWithCount(1), stack.getCount()));
        }
    }

    private record BasinKey(ResourceKey<Level> dimension, BlockPos pos) {}
    private record BatchKey(BasinKey basin, Item target) {}
    private record ExactStackCount(ItemStack stack, int count) {}
    private record FinishRequest(List<ExactStackCount> inputs, int count, boolean uniform,
                                 int incompatibleCount, ItemStack template) {}
    private record BatchPlan(List<ExactStackCount> inputs, List<ItemStack> outputs, int batchSize,
                             int target, int signalStrength, double averageQuality, int outputQuality,
                             boolean finishCommand, int incompatibleCount) {}
    private record QualityPreview(double averageQuality, int outputQuality) {}
    private record ExtractedStack(IItemHandler inventory, ItemStack stack) {}
    private record ExtractionResult(boolean success, List<ExtractedStack> extracted) {
        private static final ExtractionResult FAILURE = new ExtractionResult(false, List.of());
    }

    public record HomogenizationStatus(boolean relevant, int count, int target, double averageQuality,
                                       int predictedQuality, boolean ready, int signalStrength,
                                       boolean processing, boolean finishMode, boolean finishArmed,
                                       int incompatibleCount, boolean uniform) {
        public static final HomogenizationStatus NONE =
                new HomogenizationStatus(false, 0, 64, 0.0D, 0, false, 0,
                        false, false, false, 0, false);
    }

    public record InventoryQualitySummary(boolean present, int count, double averageQuality,
                                          int predictedQuality, boolean compatible, ItemStack sample) {
        public static final InventoryQualitySummary EMPTY =
                new InventoryQualitySummary(false, 0, 0.0D, 0, false, ItemStack.EMPTY);
    }
}
