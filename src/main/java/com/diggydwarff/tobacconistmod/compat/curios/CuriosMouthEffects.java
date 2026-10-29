package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.network.MouthEffectStatePayload;
import com.diggydwarff.tobacconistmod.util.MouthEffectProfile;
import com.diggydwarff.tobacconistmod.vapor.VaporData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import com.diggydwarff.tobacconistmod.backport.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Change notification plus a one-second heartbeat also initializes newly tracking clients. */
@EventBusSubscriber(modid=TobacconistMod.MODID)
public final class CuriosMouthEffects {
    private record State(MouthEffectStatePayload payload,Object level,long sent) {}
    private static final Map<UUID,State> SENT=new HashMap<>();
    private CuriosMouthEffects() {}
    /** Restore the reported hidden mouth slot once; later visibility choices are retained. */
    public static void repairLegacyVisibility(ServerPlayer player) {
        String key="TobacconistMouthRenderRepair13";
        var persisted=player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
        if(persisted.getBoolean(key)) return;
        var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElse(null);
        var mouth=inventory==null ? null : inventory.getCurios().get("mouth");
        if(mouth==null) return;
        var renders=mouth.getRenders();boolean changed=false;
        for(int i=0;i<renders.size();i++) {
            var stack=mouth.getStacks().getStackInSlot(i);
            if((stack.isEmpty() || CuriosSmokingHelper.supported(stack)) && !renders.get(i)) {
                renders.set(i,true);changed=true;
            }
        }
        // Curios' public updating-inventory set synchronizes the full slot tag, including renders.
        if(changed) inventory.getUpdatingInventories().add(mouth);
        persisted.putBoolean(key,true);
        player.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,persisted);
    }
    public static MouthEffectStatePayload state(ServerPlayer player) {
        var stack=CuriosSmokingHelper.visible(player).stack();
        var profile=player.isAlive() && !player.isSpectator()?MouthEffectProfile.of(stack):MouthEffectProfile.NONE;
        var blend=VaporData.blend(stack);
        return new MouthEffectStatePayload(player.getUUID(),profile.ordinal(),blend==null?0:blend.vegetablePercent());
    }
    @SubscribeEvent public static void tick(PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player) || !ModList.get().isLoaded("curios")) return;
        repairLegacyVisibility(player);
        var payload=state(player);var before=SENT.get(player.getUUID());long now=player.level().getGameTime();
        if(before==null || !before.payload.equals(payload) || before.level!=player.level()
                || payload.profile()!=MouthEffectProfile.NONE.ordinal() && now-before.sent>=20) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,payload);
            SENT.put(player.getUUID(),new State(payload,player.level(),now));
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { SENT.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { SENT.clear(); }
}
