package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class TobacconistNetwork {
    private static final String PROTOCOL = "15";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(TobacconistMod.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();
    private static int nextId;

    private TobacconistNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(nextId++, SmokeMouthItemPayload.class,
                (packet, buffer) -> SmokeMouthItemPayload.STREAM_CODEC.encode(buffer, packet),
                buffer -> SmokeMouthItemPayload.STREAM_CODEC.decode(buffer),
                (packet, supplied) -> {
                    var context = supplied.get();
                    context.enqueueWork(() -> { if (context.getSender() != null) NetworkEvents.handleMouth(context.getSender(), packet); });
                    context.setPacketHandled(true);
                }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, ProductionMonitorConfigPayload.class,
                ProductionMonitorConfigPayload::encode, ProductionMonitorConfigPayload::decode,
                ProductionMonitorConfigPayload::handle);
        CHANNEL.registerMessage(nextId++, ProductionMonitorResetPayload.class,
                ProductionMonitorResetPayload::encode, ProductionMonitorResetPayload::decode,
                ProductionMonitorResetPayload::handle);
        clientMessage(MouthEffectStatePayload.class, MouthEffectStatePayload.CODEC);
        clientMessage(SmokingDrawStatePayload.class, SmokingDrawStatePayload.STREAM_CODEC);
        clientMessage(com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload.class, com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload.CODEC);
        clientMessage(com.diggydwarff.tobacconistmod.novelty.NoveltyPuffPayload.class, com.diggydwarff.tobacconistmod.novelty.NoveltyPuffPayload.CODEC);
    }

    private static <T> void clientMessage(Class<T> type,
            com.diggydwarff.tobacconistmod.backport.StreamCodec<? super net.minecraft.network.FriendlyByteBuf,T> codec) {
        CHANNEL.registerMessage(nextId++, type, (packet, buffer) -> codec.encode(buffer, packet), codec::decode,
                (packet, supplied) -> {
                    var context = supplied.get();
                    context.enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                            net.minecraftforge.api.distmarker.Dist.CLIENT,
                            () -> () -> com.diggydwarff.tobacconistmod.client.ClientPacketHandlers.handle(packet)));
                    context.setPacketHandled(true);
                }, java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendTracking(net.minecraft.world.entity.Entity entity, Object packet) {
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }

    public static void sendSmokeRequest() {
        CHANNEL.sendToServer(SmokeMouthItemPayload.START);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
