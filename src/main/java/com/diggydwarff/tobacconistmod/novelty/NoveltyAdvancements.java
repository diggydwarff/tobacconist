package com.diggydwarff.tobacconistmod.novelty;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class NoveltyAdvancements {
    private NoveltyAdvancements() {}
    public static void ringmaster(ServerPlayer player) {
        var milestone = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "novelty/ringmaster"));
        if (milestone != null) player.getAdvancements().award(milestone, "perform");
    }
}
