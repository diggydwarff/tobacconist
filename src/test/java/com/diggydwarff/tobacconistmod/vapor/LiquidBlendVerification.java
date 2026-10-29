package com.diggydwarff.tobacconistmod.vapor;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Standalone invariant checks; run with Java 21 and assertions enabled. */
public final class LiquidBlendVerification {
    public static void main(String[] args) {
        LiquidBlend base = LiquidBlend.mix(LiquidBlend.VEGETABLE, 70, LiquidBlend.CLEAR, 30);
        require(base.vegetablePercent() == 70 && base.strength() == 0, "70/30 nicotine-free carrier");
        LiquidBlend low = LiquidBlend.mix(base, 245, LiquidBlend.NICOTINE, 5);
        require(low.nicotine() == 20_000 && low.strength() == 1 && low.usable(), "Small nicotine dose");
        LiquidBlend berry = LiquidBlend.mix(low, 240, LiquidBlend.essence("berry"), 10);
        LiquidBlend diluted = LiquidBlend.mix(berry, 250, base, 250);
        require(diluted.nicotine() == berry.nicotine() / 2, "Dilution must halve concentration");
        require(diluted.flavors().get("berry") == berry.flavors().get("berry") / 2, "Flavor amount is conserved");
        require(!LiquidBlend.NICOTINE.usable() && !LiquidBlend.essence("berry").usable(), "Concentrates cannot be vaped");

        Map<String, Integer> mutable = new HashMap<>(); mutable.put("berry", LiquidBlend.SCALE);
        LiquidBlend immutable = new LiquidBlend(0, 0, 0, mutable); mutable.clear();
        require(immutable.flavors().size() == 1, "Blend must own immutable flavor data");
        LiquidBlend two = LiquidBlend.mix(LiquidBlend.essence("berry"), 10, LiquidBlend.essence("vanilla"), 10);
        LiquidBlend three = LiquidBlend.mix(two, 20, LiquidBlend.essence("mint"), 10);
        try { LiquidBlend.mix(three, 30, LiquidBlend.essence("apple"), 10); throw new AssertionError("Fourth flavor accepted"); }
        catch (IllegalArgumentException expected) {}
        try { new LiquidBlend(1, 0, 0, Map.of()); throw new AssertionError("Non-conserving blend accepted"); }
        catch (IllegalArgumentException expected) {}

        Random random = new Random(42);
        for (int i = 0; i < 5000; i++) {
            int a = 1 + random.nextInt(2000), b = 1 + random.nextInt(2000);
            LiquidBlend mixed = LiquidBlend.mix(berry, a, base, b);
            int total = mixed.vegetable() + mixed.clear() + mixed.nicotine() + mixed.flavors().values().stream().mapToInt(Integer::intValue).sum();
            require(total == LiquidBlend.SCALE, "Repeated mixing must conserve every constituent");
            long expectedNicotine = (long) berry.nicotine() * a / (a + b);
            require(Math.abs(mixed.nicotine() - expectedNicotine) <= 3, "Volume-weighted nicotine remains bounded");
            require(mixed.flavors().keySet().equals(berry.flavors().keySet()), "Mixing must retain flavor identity");
        }
        System.out.println("Liquid blend verification passed: dilution, conservation, immutable data, flavor limits, and 5000 randomized mixtures.");
    }
    private static void require(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
}
