package com.diggydwarff.tobacconistmod.backport;

import com.diggydwarff.tobacconistmod.network.TobacconistNetwork;
import net.minecraft.world.entity.Entity;
public final class PacketDistributor {
    private PacketDistributor() {}
    public static void sendToServer(Object packet) { TobacconistNetwork.sendToServer(packet); }
    public static void sendToPlayersTrackingEntityAndSelf(Entity entity,Object packet) { TobacconistNetwork.sendTracking(entity,packet); }
}
