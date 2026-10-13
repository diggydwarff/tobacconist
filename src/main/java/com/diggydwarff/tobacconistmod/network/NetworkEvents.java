package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.client.ClientDrawAnimations;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import net.minecraft.world.item.ItemStack;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import com.diggydwarff.tobacconistmod.block.entity.ProductionMonitorBlockEntity;
import com.diggydwarff.tobacconistmod.screen.ProductionMonitorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class NetworkEvents {
    private static final long MANUAL_SMOKE_COOLDOWN_TICKS = 8L;
    private static final Map<UUID, Long> LAST_MANUAL_SMOKE_TICK = new HashMap<>();

    private NetworkEvents() {}

    @SubscribeEvent
    public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_MANUAL_SMOKE_TICK.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void changedDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        LAST_MANUAL_SMOKE_TICK.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        LAST_MANUAL_SMOKE_TICK.clear();
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("15");
        registrar.playToClient(MouthEffectStatePayload.TYPE,MouthEffectStatePayload.CODEC,
                (payload, context) -> com.diggydwarff.tobacconistmod.client.ClientSmokingParticles.receive(payload));
        registrar.playToClient(com.diggydwarff.tobacconistmod.novelty.NoveltyPuffPayload.TYPE,
                com.diggydwarff.tobacconistmod.novelty.NoveltyPuffPayload.CODEC,
                (payload, context) -> com.diggydwarff.tobacconistmod.novelty.client.NoveltyParticle.puff(payload));
        registrar.playToClient(com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload.TYPE,
                com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload.CODEC,
                (payload, context) -> com.diggydwarff.tobacconistmod.vapor.client.VaporParticle.puff(payload));
        registrar.playToServer(
                SmokeMouthItemPayload.TYPE,
                SmokeMouthItemPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;

                    if (!ModList.get().isLoaded("curios")) {
                        player.displayClientMessage(Component.translatable("tobacconistmod.message.mouth_slot_requires_curios"), true);
                        return;
                    }

                    if (!payload.drawing()) {
                        if (payload.cancel()) SmokingDraws.cancelMouth(player);
                        else SmokingDraws.releaseMouth(player);
                        return;
                    }
                    if (TobacconistConfig.areSmokingDrawAnimationsEnabled()) {
                        ItemStack stack = CuriosSmokingHelper.getMouthItem(player);
                        if (!CuriosSmokingHelper.supported(stack)) {
                            player.displayClientMessage(Component.translatable("tobacconistmod.message.mouth.empty"), true);
                        } else if (!CuriosSmokingHelper.canDraw(stack)) {
                            player.displayClientMessage(Component.translatable("tobacconistmod.message.mouth.no_puffs"), true);
                        } else {
                            SmokingDraws.startMouth(player, stack);
                        }
                        return;
                    }

                    if (!SmokingDraws.canStart(player, CuriosSmokingHelper.getMouthItem(player))) return;
                    long now = player.serverLevel().getGameTime();
                    long last = LAST_MANUAL_SMOKE_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2);
                    if (now - last < MANUAL_SMOKE_COOLDOWN_TICKS) {
                        return;
                    }

                    Component failure = CuriosSmokingHelper.trySmokeMouthItem(player);
                    if (failure == null) {
                        LAST_MANUAL_SMOKE_TICK.put(player.getUUID(), now);
                    } else {
                        player.displayClientMessage(failure, true);
                    }
                }
        );

        registrar.playToClient(
                SmokingDrawStatePayload.TYPE,
                SmokingDrawStatePayload.STREAM_CODEC,
                (payload, context) -> ClientDrawAnimations.receiveMouthDraw(payload.entityId(), payload.ticks())
        );

        registrar.playToServer(
                ProductionMonitorConfigPayload.TYPE,
                ProductionMonitorConfigPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)
                            || !(player.containerMenu instanceof ProductionMonitorMenu menu)
                            || !menu.getBlockPos().equals(payload.pos())
                            || player.distanceToSqr(payload.pos().getX() + 0.5D, payload.pos().getY() + 0.5D,
                                    payload.pos().getZ() + 0.5D) > 64.0D) {
                        return;
                    }
                    if (player.serverLevel().getBlockEntity(payload.pos()) instanceof ProductionMonitorBlockEntity monitor) {
                        monitor.applyConfiguration(payload.target(), payload.countMode(), payload.atTarget(),
                                payload.output(), payload.externalReset(), payload.filter());
                    }
                }
        );

        registrar.playToServer(
                ProductionMonitorResetPayload.TYPE,
                ProductionMonitorResetPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)
                            || !(player.containerMenu instanceof ProductionMonitorMenu menu)
                            || !menu.getBlockPos().equals(payload.pos())
                            || player.distanceToSqr(payload.pos().getX() + 0.5D, payload.pos().getY() + 0.5D,
                                    payload.pos().getZ() + 0.5D) > 64.0D) {
                        return;
                    }
                    if (player.serverLevel().getBlockEntity(payload.pos()) instanceof ProductionMonitorBlockEntity monitor) {
                        monitor.resetAccumulatedCount();
                    }
                }
        );
    }
}
