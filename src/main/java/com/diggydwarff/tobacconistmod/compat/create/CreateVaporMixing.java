package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.vapor.*;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import java.util.*;

/** Staged mixing supports arbitrary existing essences without registering every combination. */
public final class CreateVaporMixing {
    private CreateVaporMixing() {}

    public static boolean apply(BasinBlockEntity basin, boolean simulate) {
        if (basin.getLevel() == null || basin.inputTank == null) return false;
        LiquidBlend target = null;
        var items = basin.getInputInventory();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (!stack.is(VaporContent.RECIPE_CARD.get()) || target != null) return false;
            target = VaporData.blend(stack);
        }
        if (target == null || !target.usable()) return false;
        IFluidHandler input = basin.inputTank.getCapability();
        List<FluidStack> available = new ArrayList<>();
        for (int i = 0; i < input.getTanks(); i++) {
            FluidStack stack = input.getFluidInTank(i);
            if (!stack.isEmpty()) available.add(stack.copy());
        }
        if (available.size() == 1) {
            FluidStack source = available.get(0);
            if (source.getFluid() == VaporContent.LIQUID_FLUID.get() || !target.equals(VaporData.blend(source)) || source.getAmount() < 250) return false;
            FluidStack output = VaporData.fluid(target, 250);
            if (basin.getFilter() != null && !basin.getFilter().test(output) || !basin.acceptOutputs(List.of(), List.of(output), true)) return false;
            basin.inputTank.allowExtraction();
            try {
                FluidStack need = source.copyWithAmount(250);
                if (input.drain(need, IFluidHandler.FluidAction.SIMULATE).getAmount() != 250) return false;
                if (simulate) return true;
                FluidStack consumed = input.drain(need, IFluidHandler.FluidAction.EXECUTE);
                if (consumed.getAmount() != 250 || !basin.acceptOutputs(List.of(), List.of(output), false)) {
                    input.fill(consumed, IFluidHandler.FluidAction.EXECUTE); return false;
                }
                basin.inputTank.sendDataImmediately(); basin.notifyChangeOfContents(); return true;
            } finally { basin.inputTank.forbidExtraction(); }
        }
        if (available.size() != 2) return false;
        LiquidBlend a = VaporData.blend(available.get(0)), b = VaporData.blend(available.get(1));
        if (a == null || b == null) return false;
        Map<String, Integer> targetParts = parts(target), aParts = parts(a), bParts = parts(b);
        // Each stage introduces a distinct constituent/group, avoiding accidental self-reprocessing.
        if (!Collections.disjoint(aParts.keySet(), bParts.keySet())
                || !targetParts.keySet().containsAll(aParts.keySet()) || !targetParts.keySet().containsAll(bParts.keySet())) return false;
        if (!matchesSubset(a, targetParts, aParts.keySet()) || !matchesSubset(b, targetParts, bParts.keySet())) return false;
        int amountA = dose(targetParts, aParts.keySet()), amountB = dose(targetParts, bParts.keySet());
        int consumeA = consumption(a, amountA), consumeB = consumption(b, amountB);
        if (amountA <= 0 || amountB <= 0 || available.get(0).getAmount() < consumeA || available.get(1).getAmount() < consumeB) return false;
        Set<String> combined = new TreeSet<>(aParts.keySet()); combined.addAll(bParts.keySet());
        LiquidBlend result = subset(targetParts, combined);
        FluidStack output = VaporData.fluid(result, amountA + amountB);
        if (basin.getFilter() != null && !basin.getFilter().test(output)) return false;
        if (!basin.acceptOutputs(List.of(), List.of(output), true)) return false;
        basin.inputTank.allowExtraction();
        try {
            FluidStack needA = available.get(0).copyWithAmount(consumeA), needB = available.get(1).copyWithAmount(consumeB);
            if (input.drain(needA, IFluidHandler.FluidAction.SIMULATE).getAmount() != consumeA
                    || input.drain(needB, IFluidHandler.FluidAction.SIMULATE).getAmount() != consumeB) return false;
            if (simulate) return true;
            FluidStack consumedA = input.drain(needA, IFluidHandler.FluidAction.EXECUTE);
            FluidStack consumedB = input.drain(needB, IFluidHandler.FluidAction.EXECUTE);
            if (consumedA.getAmount() != consumeA || consumedB.getAmount() != consumeB
                    || !basin.acceptOutputs(List.of(), List.of(output.copy()), false)) {
                input.fill(consumedA, IFluidHandler.FluidAction.EXECUTE);
                input.fill(consumedB, IFluidHandler.FluidAction.EXECUTE);
                return false;
            }
            basin.inputTank.sendDataImmediately(); basin.notifyChangeOfContents(); return true;
        } finally { basin.inputTank.forbidExtraction(); }
    }
    private static int consumption(LiquidBlend input, int contribution) {
        // Condense one full essence dose into the selected flavor contribution.
        if (input.vegetable() == 0 && input.clear() == 0 && input.nicotine() == 0 && input.flavors().size() == 1) {
            return Math.max(1, (contribution + 9) / 10) * VaporData.BOTTLE;
        }
        return contribution;
    }
    private static int dose(Map<String, Integer> parts, Set<String> keys) {
        int sum = keys.stream().mapToInt(parts::get).sum();
        return sum % 4000 == 0 ? sum / 4000 : -1;
    }
    private static boolean matchesSubset(LiquidBlend input, Map<String, Integer> target, Set<String> keys) {
        Map<String, Integer> expected = parts(subset(target, keys)), actual = parts(input);
        return expected.keySet().equals(actual.keySet()) && expected.entrySet().stream().allMatch(entry -> Math.abs(entry.getValue() - actual.get(entry.getKey())) <= 4);
    }
    private static Map<String, Integer> parts(LiquidBlend blend) {
        Map<String, Integer> result = new TreeMap<>(blend.flavors());
        if (blend.vegetable() > 0) result.put("@vegetable", blend.vegetable());
        if (blend.clear() > 0) result.put("@clear", blend.clear());
        if (blend.nicotine() > 0) result.put("@nicotine", blend.nicotine());
        return result;
    }
    private static LiquidBlend subset(Map<String, Integer> target, Set<String> keys) {
        long total = keys.stream().mapToLong(target::get).sum();
        Map<String, Integer> normalized = new TreeMap<>();
        keys.forEach(key -> normalized.put(key, (int) ((long) target.get(key) * LiquidBlend.SCALE / total)));
        int residue = LiquidBlend.SCALE - normalized.values().stream().mapToInt(Integer::intValue).sum();
        String largest = normalized.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        normalized.merge(largest, residue, Integer::sum);
        int vg = normalized.getOrDefault("@vegetable", 0), pg = normalized.getOrDefault("@clear", 0), nicotine = normalized.getOrDefault("@nicotine", 0);
        normalized.keySet().removeIf(key -> key.startsWith("@"));
        return new LiquidBlend(vg, pg, nicotine, normalized);
    }
}
