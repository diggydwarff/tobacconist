package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Server-side Curios integration kept separate so the optional Curios dependency is only touched when loaded. */
public final class CuriosSmokingHelper {
    private CuriosSmokingHelper() {}
    public static boolean supported(ItemStack stack) {
        return stack.getItem() instanceof SmokingItem || com.diggydwarff.tobacconistmod.vapor.VapePenItem.isCigalike(stack)
                || stack.getItem() instanceof com.diggydwarff.tobacconistmod.vapor.BubblePipeItem
                || stack.getItem() instanceof com.diggydwarff.tobacconistmod.novelty.ToyPipeItem;
    }
    public static boolean canDraw(ItemStack stack) {
        if (stack.getItem() instanceof com.diggydwarff.tobacconistmod.novelty.ToyPipeItem) return com.diggydwarff.tobacconistmod.novelty.ToyPipeItem.canBlow(stack);
        if (stack.getItem() instanceof com.diggydwarff.tobacconistmod.vapor.BubblePipeItem) return com.diggydwarff.tobacconistmod.vapor.BubblePipeItem.blows(stack) > 0;
        return stack.getItem() instanceof SmokingItem smoking ? smoking.canSmoke(stack)
                : com.diggydwarff.tobacconistmod.vapor.VapePenItem.canUseFromMouth(stack);
    }

    public record MouthSlotEntry(int index, ItemStack stack, boolean cosmetic) {}
    /** The first functional mouth item is the one used by the mouth key. */
    public static MouthSlotEntry functional(net.minecraft.world.entity.LivingEntity player) {
        var inventory=CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
        var mouth=inventory==null ? null : inventory.getCurios().get("mouth");
        if (mouth!=null) for(int i=0;i<mouth.getStacks().getSlots();i++) {
            var stack=mouth.getStacks().getStackInSlot(i);
            if (!stack.isEmpty() && supported(stack)) return new MouthSlotEntry(i,stack,false);
        }
        return new MouthSlotEntry(-1,ItemStack.EMPTY,false);
    }
    /** Cosmetics override functional items. Missing render flags use the original visible default. */
    public static MouthSlotEntry visible(net.minecraft.world.entity.LivingEntity player) {
        var inventory=CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
        var mouth=inventory==null ? null : inventory.getCurios().get("mouth");
        if(mouth!=null) for(int i=0;i<mouth.getStacks().getSlots();i++) {
            var cosmetic=i<mouth.getCosmeticStacks().getSlots() ? mouth.getCosmeticStacks().getStackInSlot(i) : ItemStack.EMPTY;
            if(!cosmetic.isEmpty()) {
                if(supported(cosmetic)) return new MouthSlotEntry(i,cosmetic,true);
                continue;
            }
            var renders=mouth.getRenders();
            if(i<renders.size() && !renders.get(i)) continue;
            var stack=mouth.getStacks().getStackInSlot(i);
            if(!stack.isEmpty() && supported(stack)) return new MouthSlotEntry(i,stack,false);
        }
        return new MouthSlotEntry(-1,ItemStack.EMPTY,false);
    }
    public static ItemStack getMouthItem(ServerPlayer player) { return functional(player).stack(); }

    /**
     * Attempts one deliberate puff. Returns {@code null} on success or a short
     * player-facing failure message when no puff could be taken.
     */
    public static Component trySmokeMouthItem(ServerPlayer player) {
        return trySmokeMouthItem(player, 1.0F);
    }

    public static Component trySmokeMouthItem(ServerPlayer player, float strength) {
        if (!com.diggydwarff.tobacconistmod.util.SmokingDraws.canFinish(player) || player.isUsingItem())
            return Component.translatable("tobacconistmod.message.mouth.unavailable");
        final Component[] result = {Component.translatable("tobacconistmod.message.mouth.no_slot")};

        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(inventory -> {
            var mouth = inventory.getCurios().get("mouth");
            if (mouth == null || mouth.getStacks().getSlots() <= 0) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.no_slot");
                return;
            }

            var entry = functional(player);
            ItemStack stack = entry.stack();
            if (stack.isEmpty()) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.empty");
                return;
            }
            if (!supported(stack)) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.not_smokable");
                return;
            }

            // A deliberate mouth-key tap has the same minimum as normal pipes.
            // Toys and the compact vape still receive a valid small draw internally.
            float puffStrength = strength;
            if (strength > 0F) {
                if (com.diggydwarff.tobacconistmod.vapor.VapePenItem.isCigalike(stack)) puffStrength = Math.max(strength, 6F / 40F);
                else if (stack.getItem() instanceof com.diggydwarff.tobacconistmod.vapor.BubblePipeItem
                        || stack.getItem() instanceof com.diggydwarff.tobacconistmod.novelty.ToyPipeItem) puffStrength = Math.max(strength, 4F / 30F);
            }
            boolean used = stack.getItem() instanceof SmokingItem smokingItem
                    ? smokingItem.smokeFromMouthSlot(player, player.getLevel(), stack, puffStrength)
                    : stack.getItem() instanceof com.diggydwarff.tobacconistmod.novelty.ToyPipeItem novelty
                        ? novelty.blowFromMouth(player, stack, puffStrength)
                        : stack.getItem() instanceof com.diggydwarff.tobacconistmod.vapor.BubblePipeItem toy
                        ? toy.blowFromMouth(player, stack, puffStrength)
                        : ((com.diggydwarff.tobacconistmod.vapor.VapePenItem) stack.getItem()).puffFromMouth(player, stack, puffStrength);
            if (!used) {
                result[0] = Component.translatable("tobacconistmod.message.mouth.no_puffs");
                return;
            }

            // Reinsert to notify Curios that NBT/damage/count changed and synchronize the slot.
            mouth.getStacks().setStackInSlot(entry.index(), stack.isEmpty() ? ItemStack.EMPTY : stack);
            result[0] = null;
        });

        return result[0];
    }
}
