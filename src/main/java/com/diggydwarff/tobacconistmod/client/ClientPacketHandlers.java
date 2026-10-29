package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.network.*;
import com.diggydwarff.tobacconistmod.vapor.VaporPuffPayload;
import com.diggydwarff.tobacconistmod.novelty.NoveltyPuffPayload;

/** Referenced only by Forge's client-side packet work queue. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {}
    public static void handle(Object packet) {
        if (packet instanceof MouthEffectStatePayload state) ClientSmokingParticles.receive(state);
        else if (packet instanceof SmokingDrawStatePayload state) ClientDrawAnimations.receiveMouthDraw(state.entityId(),state.ticks());
        else if (packet instanceof VaporPuffPayload puff) com.diggydwarff.tobacconistmod.vapor.client.VaporParticle.puff(puff);
        else if (packet instanceof NoveltyPuffPayload puff) com.diggydwarff.tobacconistmod.novelty.client.NoveltyParticle.puff(puff);
    }
}
