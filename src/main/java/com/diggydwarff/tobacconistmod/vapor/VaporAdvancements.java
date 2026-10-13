package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Action milestones only award after successful processing or a valid draw. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class VaporAdvancements {
    private VaporAdvancements() {}

    private static net.minecraft.advancements.AdvancementHolder advancement(ServerPlayer player, String name) {
        return player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "vapor/" + name));
    }

    private static void award(ServerPlayer player, String name) {
        var advancement = advancement(player, name);
        if (advancement != null) player.getAdvancements().award(advancement, "perform");
    }

    public static void onRecipeSaved(ServerPlayer player, ItemStack card) {
        LiquidBlend blend = VaporData.blend(card);
        if (card.is(VaporContent.RECIPE_CARD.get()) && blend != null && blend.usable()) award(player, "recipe_for_success");
    }

    public static void onPuff(ServerPlayer player, LiquidBlend blend, int draw) {
        award(player, "first_exhale");
        if (blend.flavors().size() >= 2) award(player, "flavor_composer");
        if (blend.nicotine() == 0 && !blend.flavors().isEmpty()) award(player, "just_the_flavor");
        if (blend.vegetablePercent() >= 80 && draw >= 40) award(player, "cloud_chaser");
    }
    public static void onBubbles(ServerPlayer player) { award(player, "pipe_dream"); }

    public static void scanInventory(ServerPlayer player) {
        var advancement = advancement(player, "small_batch");
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) return;
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!VaporData.isLiquidBottle(stack) || VaporData.volume(stack) == 0) continue;
            LiquidBlend blend = VaporData.blend(stack);
            if (blend != null && blend.usable()) {
                award(player, "small_batch");
                return;
            }
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                && player.tickCount % 20 == 0) scanInventory(player);
    }
}
