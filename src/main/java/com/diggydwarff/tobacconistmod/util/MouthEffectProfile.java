package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.novelty.RelicPipeItem;
import com.diggydwarff.tobacconistmod.novelty.ToyPipeItem;
import com.diggydwarff.tobacconistmod.vapor.BubblePipeItem;
import com.diggydwarff.tobacconistmod.vapor.VapePenItem;
import net.minecraft.world.item.ItemStack;

/** Cosmetic idle effects read synced item contents, without consuming resources or applying effects. */
public enum MouthEffectProfile {
    NONE(false), SMOKE(true), VAPOR(false), BUBBLES(false), CONFETTI(false), WIZARD(true), SOUL(true), ASTRAL(true), PETALS(false), NOTES(false), SUN(true), ECHO(true);
    private final boolean smoke;
    MouthEffectProfile(boolean smoke) { this.smoke = smoke; }
    public boolean smoke() { return smoke; }
    public static MouthEffectProfile of(ItemStack stack) {
        if (stack.isEmpty()) return NONE;
        if (VapePenItem.isCigalike(stack)) return VapePenItem.canUseFromMouth(stack) ? VAPOR : NONE;
        if (stack.getItem() instanceof BubblePipeItem) return BubblePipeItem.blows(stack) > 0 ? BUBBLES : NONE;
        if (stack.getItem() instanceof ToyPipeItem toy) {
            if(!ToyPipeItem.canBlow(stack)) return NONE;
            return switch(toy.kind()) { case PETAL -> PETALS; case WHISTLE -> NOTES; default -> CONFETTI; };
        }
        if (stack.getItem() instanceof RelicPipeItem relic) {
            if (!relic.canSmoke(stack)) return NONE;
            return switch (relic.kind()) { case WIZARD -> WIZARD; case SOUL -> SOUL; case ASTRAL -> ASTRAL; case SUN -> SUN; case ECHO -> ECHO; default -> NONE; };
        }
        return stack.getItem() instanceof SmokingItem item && item.canSmoke(stack) ? SMOKE : NONE;
    }
    /** Short cosmetic exhalations separated by 6–8 seconds; UUIDs stagger nearby players. */
    public int idlePuffStep(long time, java.util.UUID player, int density, boolean minimal) {
        if (this == NONE) return -1;
        if (this == SMOKE || this == VAPOR)
            return Math.floorMod(time + player.hashCode(), interval(density, minimal)) == 0 ? 0 : -1;
        int seed = player.hashCode();
        int period = 120 + Math.floorMod(seed, 41);
        int phase = (int)Math.floorMod(time + Math.floorMod(seed, period), period);
        int steps = minimal || density > 16 ? 1 : density > 8 ? 2 : 3;
        return phase % 6 == 0 && phase / 6 < steps ? phase / 6 : -1;
    }
    public static int interval(int density, boolean minimal) {
        return com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(6 + density, 8, 30) * (minimal ? 2 : 1);
    }
}
