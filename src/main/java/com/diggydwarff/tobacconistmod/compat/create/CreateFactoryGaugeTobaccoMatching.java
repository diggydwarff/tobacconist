package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.util.LegacyItemTags;
import com.diggydwarff.tobacconistmod.util.TobaccoCuringHelper;
import com.diggydwarff.tobacconistmod.util.TobaccoProductQualityHelper;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Factory Gauge matching that normalizes numeric quality within a quality tier while requiring
 * variety, cure, cut, flavor, blend, wrapper, and other product metadata to match exactly.
 */
public final class CreateFactoryGaugeTobaccoMatching {
    private CreateFactoryGaugeTobaccoMatching() {}

    public static boolean supportsTierMatching(ItemStack stack) {
        return !stack.isEmpty() && !CreateTobaccoItemAttribute.collect(stack).isEmpty();
    }

    public static boolean matchesByQualityTier(ItemStack requested, ItemStack candidate) {
        if (requested.isEmpty() || candidate.isEmpty() || requested.getItem() != candidate.getItem()) {
            return false;
        }
        if (!supportsTierMatching(requested) || !supportsTierMatching(candidate)) {
            return ItemStack.isSameItemSameTags(requested, candidate);
        }

        ItemStack normalizedRequested = normalizedCopy(requested);
        ItemStack normalizedCandidate = normalizedCopy(candidate);
        return ItemStack.isSameItemSameTags(normalizedRequested, normalizedCandidate);
    }

    public static int getCountOf(InventorySummary summary, ItemStack requested) {
        if (!supportsTierMatching(requested)) {
            return summary.getCountOf(requested);
        }
        return summary.getTotalOfMatching(stack -> matchesByQualityTier(requested, stack));
    }

    public static int getStockOf(UUID network, ItemStack requested, @Nullable IdentifiedInventory ignoredInventory) {
        if (!supportsTierMatching(requested)) {
            return 0;
        }

        int total = 0;
        for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(network, false)) {
            total += getCountOf(link.getSummary(ignoredInventory), requested);
        }
        return total;
    }

    /** Resolves a tier request to exact stock variants for component-exact packager extraction. */
    public static PackageOrderWithCrafts resolveRestockOrder(UUID network,
                                                              PackageOrderWithCrafts original,
                                                              @Nullable IdentifiedInventory ignoredInventory) {
        List<BigItemStack> requestedStacks = original.stacks();
        if (requestedStacks.size() != 1) {
            return original;
        }

        BigItemStack requested = requestedStacks.get(0);
        if (requested.count <= 0 || !supportsTierMatching(requested.stack)) {
            return original;
        }

        ArrayList<BigItemStack> candidates = new ArrayList<>();
        for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(network, true)) {
            InventorySummary summary = link.getSummary(ignoredInventory);
            for (BigItemStack entry : summary.getStacks()) {
                if (entry.count <= 0 || !matchesByQualityTier(requested.stack, entry.stack)) {
                    continue;
                }
                mergeExact(candidates, entry);
            }
        }

        if (candidates.isEmpty()) {
            return original;
        }

        // Prefer the exact Gauge sample when it exists; otherwise pull the most plentiful matching
        // quality variant first. Multiple exact variants may be included when necessary to satisfy
        // the requested tier amount.
        candidates.sort(Comparator
                .comparing((BigItemStack entry) -> !ItemStack.isSameItemSameTags(entry.stack, requested.stack))
                .thenComparing((BigItemStack entry) -> entry.count, Comparator.reverseOrder()));

        int remaining = requested.count;
        ArrayList<BigItemStack> resolved = new ArrayList<>();
        for (BigItemStack candidate : candidates) {
            if (remaining <= 0) break;
            int amount = Math.min(remaining, candidate.count);
            if (amount <= 0) continue;
            resolved.add(new BigItemStack(candidate.stack.copyWithCount(1), amount));
            remaining -= amount;
        }

        return remaining == 0 && !resolved.isEmpty()
                ? PackageOrderWithCrafts.simple(resolved)
                : original;
    }

    private static void mergeExact(List<BigItemStack> candidates, BigItemStack incoming) {
        for (BigItemStack existing : candidates) {
            if (!ItemStack.isSameItemSameTags(existing.stack, incoming.stack)) continue;
            existing.count += incoming.count;
            return;
        }
        candidates.add(new BigItemStack(incoming.stack.copyWithCount(1), incoming.count));
    }

    private static ItemStack normalizedCopy(ItemStack stack) {
        ItemStack copy = stack.copyWithCount(1);
        CompoundTag tag = LegacyItemTags.getTag(copy);
        if (tag == null) {
            return copy;
        }

        normalizeQualityTags(tag);
        LegacyItemTags.setTag(copy, tag);
        return copy;
    }

    private static void normalizeQualityTags(CompoundTag tag) {
        for (String key : new ArrayList<>(tag.getAllKeys())) {
            Tag value = tag.get(key);
            if (value instanceof CompoundTag nested) {
                normalizeQualityTags(nested);
            } else if (value instanceof ListTag list) {
                for (Tag element : list) {
                    if (element instanceof CompoundTag nestedElement) {
                        normalizeQualityTags(nestedElement);
                    }
                }
            }
        }

        if (tag.contains(TobaccoCuringHelper.TAG_GROWTH_QUALITY)) {
            int quality = tag.getInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY);
            tag.putInt(TobaccoCuringHelper.TAG_GROWTH_QUALITY, canonicalRawQuality(quality));
        }
        if (tag.contains(TobaccoCuringHelper.TAG_QUALITY)) {
            int quality = tag.getInt(TobaccoCuringHelper.TAG_QUALITY);
            tag.putInt(TobaccoCuringHelper.TAG_QUALITY, TobaccoCuringHelper.getCanonicalTierQuality(quality));
            tag.putString(TobaccoCuringHelper.TAG_QUALITY_TIER, TobaccoCuringHelper.getQualityTierId(quality));
        }
        if (tag.contains(TobaccoProductQualityHelper.TAG_INPUT_TOBACCO_QUALITY)) {
            int quality = tag.getInt(TobaccoProductQualityHelper.TAG_INPUT_TOBACCO_QUALITY);
            tag.putInt(TobaccoProductQualityHelper.TAG_INPUT_TOBACCO_QUALITY,
                    TobaccoCuringHelper.getCanonicalTierQuality(quality));
        }
        if (tag.contains(TobaccoProductQualityHelper.TAG_PRODUCT_QUALITY)) {
            int quality = tag.getInt(TobaccoProductQualityHelper.TAG_PRODUCT_QUALITY);
            tag.putInt(TobaccoProductQualityHelper.TAG_PRODUCT_QUALITY, canonicalProductQuality(quality));
        }
    }

    private static int canonicalRawQuality(int quality) {
        int clamped = Math.max(0, Math.min(70, quality));
        if (clamped <= 15) return 8;
        if (clamped <= 30) return 23;
        if (clamped <= 45) return 38;
        if (clamped <= 59) return 52;
        return 65;
    }

    private static int canonicalProductQuality(int quality) {
        int clamped = Math.max(0, Math.min(10, quality));
        if (clamped <= 3) return 2;
        if (clamped <= 6) return 5;
        if (clamped <= 8) return 7;
        if (clamped == 9) return 9;
        return 10;
    }
}
