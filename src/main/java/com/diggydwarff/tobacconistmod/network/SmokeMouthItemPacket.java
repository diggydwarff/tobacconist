package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class SmokeMouthItemPacket {
    private static final long COOLDOWN_TICKS = 8L;
    private static final Map<UUID, Long> LAST_SMOKE_TICK = new HashMap<>();

    public static void encode(SmokeMouthItemPacket packet, FriendlyByteBuf buf) {}
    public static SmokeMouthItemPacket decode(FriendlyByteBuf buf) { return new SmokeMouthItemPacket(); }

    public static void handle(SmokeMouthItemPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!ModList.get().isLoaded("curios")) {
                player.displayClientMessage(Component.literal("Manual mouth-slot smoking requires Curios."), true);
                return;
            }
            long now = player.serverLevel().getGameTime();
            long last = LAST_SMOKE_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2);
            if (now - last < COOLDOWN_TICKS) return;
            Component failure = CuriosSmokingHelper.trySmokeMouthItem(player);
            if (failure == null) LAST_SMOKE_TICK.put(player.getUUID(), now);
            else player.displayClientMessage(failure, true);
        });
        context.setPacketHandled(true);
    }
}
