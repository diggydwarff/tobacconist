package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Decant a large/small bottle into vanilla glass without replacing its normal water behavior. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class BottlePouring {
    private BottlePouring() {}
    @SubscribeEvent public static void pour(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide || !event.getItemStack().is(Items.GLASS_BOTTLE)) return;
        InteractionHand source = event.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!(event.getEntity().getItemInHand(source).getItem() instanceof SizedBottleItem)) return;
        if (SizedBottleItem.pour(event.getEntity(), event.getHand(), source, false)) {
            event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
