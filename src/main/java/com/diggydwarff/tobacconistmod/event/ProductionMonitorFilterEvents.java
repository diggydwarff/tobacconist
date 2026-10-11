package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.custom.ProductionMonitorBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Handle the external ghost slot before sneaking or held-item use can bypass block use. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class ProductionMonitorFilterEvents {
    private ProductionMonitorFilterEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void setExternalFilter(PlayerInteractEvent.RightClickBlock event) {
        // Keep other mods' interaction denials and spectator restrictions intact.
        if (event.isCanceled() || event.getUseBlock() == TriState.FALSE || event.getEntity().isSpectator()) {
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
