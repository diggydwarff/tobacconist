package com.diggydwarff.tobacconistmod.vapor;

import java.util.Map;
import java.util.TreeMap;

/** Immutable composition. Parts include additives, so mixing always conserves total liquid. */
public record LiquidBlend(int vegetable, int clear, int nicotine, Map<String, Integer> flavors) {
    public static final int SCALE = 1_000_000;
    public static final LiquidBlend VEGETABLE = new LiquidBlend(SCALE, 0, 0, Map.of());
    public static final LiquidBlend CLEAR = new LiquidBlend(0, SCALE, 0, Map.of());
    public static final LiquidBlend NICOTINE = new LiquidBlend(0, 0, SCALE, Map.of());

    public LiquidBlend {
        flavors = Map.copyOf(new TreeMap<>(flavors));
        long total = (long) vegetable + clear + nicotine;
        if (vegetable < 0 || clear < 0 || nicotine < 0 || flavors.size() > 3) {
            throw new IllegalArgumentException("Invalid liquid composition");
        }
        for (var entry : flavors.entrySet()) {
            if (!entry.getKey().matches("[a-z0-9_]+") || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Invalid flavor");
            }
            total += entry.getValue();
        }
        if (total != SCALE) throw new IllegalArgumentException("Liquid parts must total " + SCALE);
    }

    public static LiquidBlend essence(String flavor) {
        return new LiquidBlend(0, 0, 0, Map.of(flavor, SCALE));
    }

    public int vegetablePercent() {
        int carrier = vegetable + clear;
        return carrier == 0 ? 0 : (int) Math.round(100.0 * vegetable / carrier);
    }

    public int strength() {
        if (nicotine == 0) return 0;
        return nicotine <= 25_000 ? 1 : nicotine <= 45_000 ? 2 : 3;
    }

    public boolean usable() { return vegetable + clear >= SCALE / 2 && nicotine <= 100_000; }

    public static LiquidBlend mix(LiquidBlend a, int amountA, LiquidBlend b, int amountB) {
        if (amountA <= 0 || amountB <= 0) throw new IllegalArgumentException("Empty mixing input");
        long total = (long) amountA + amountB;
        Map<String, Long> weighted = new TreeMap<>();
        a.flavors.forEach((key, value) -> weighted.put(key, (long) value * amountA));
        b.flavors.forEach((key, value) -> weighted.merge(key, (long) value * amountB, Long::sum));
        if (weighted.size() > 3) throw new IllegalArgumentException("At most three flavors per blend");
        int vg = (int) (((long) a.vegetable * amountA + (long) b.vegetable * amountB) / total);
        int pg = (int) (((long) a.clear * amountA + (long) b.clear * amountB) / total);
        int nic = (int) (((long) a.nicotine * amountA + (long) b.nicotine * amountB) / total);
        Map<String, Integer> flavors = new TreeMap<>();
        weighted.forEach((key, value) -> { if (value / total > 0) flavors.put(key, (int) (value / total)); });
        int remainder = SCALE - vg - pg - nic - flavors.values().stream().mapToInt(Integer::intValue).sum();
        // Assign rounding residue to the largest constituent, never invent carrier in an essence.
        String largestFlavor = flavors.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
        int largest = largestFlavor == null ? -1 : flavors.get(largestFlavor);
        if (vg >= pg && vg >= nic && vg >= largest) vg += remainder;
        else if (pg >= nic && pg >= largest) pg += remainder;
        else if (nic >= largest) nic += remainder;
        else flavors.merge(largestFlavor, remainder, Integer::sum);
        return new LiquidBlend(vg, pg, nic, flavors);
    }
}
