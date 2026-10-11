package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.custom.ProductionMonitorBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Handle the external ghost slot before sneaking or held-item use can bypass block use. */
@Mod.EventBusSubscriber(modid = TobacconistMod.MODID)
public final class ProductionMonitorFilterEvents {
    private ProductionMonitorFilterEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void setExternalFilter(PlayerInteractEvent.RightClickBlock event) {
        // Keep other mods' interaction denials and spectator restrictions intact.
        if (event.isCanceled() || event.getUseBlock() == Event.Result.DENY || event.getEntity().isSpectator()) {
            return;
        }
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof ProductionMonitorBlock monitor)) return;
        if (!monitor.trySetExternalFilter(state, event.getLevel(), event.getPos(), event.getItemStack(),
                event.getHand(), event.getHitVec())) {
            return;
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
    }
}
