package com.diggydwarff.tobacconistmod.network;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class TobacconistNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(TobacconistMod.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();
    private static int nextId;

    private TobacconistNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(nextId++, SmokeMouthItemPacket.class,
                SmokeMouthItemPacket::encode, SmokeMouthItemPacket::decode, SmokeMouthItemPacket::handle);
        CHANNEL.registerMessage(nextId++, ProductionMonitorConfigPayload.class,
                ProductionMonitorConfigPayload::encode, ProductionMonitorConfigPayload::decode,
                ProductionMonitorConfigPayload::handle);
        CHANNEL.registerMessage(nextId++, ProductionMonitorResetPayload.class,
                ProductionMonitorResetPayload::encode, ProductionMonitorResetPayload::decode,
                ProductionMonitorResetPayload::handle);
    }

    public static void sendSmokeRequest() {
        CHANNEL.sendToServer(new SmokeMouthItemPacket());
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
