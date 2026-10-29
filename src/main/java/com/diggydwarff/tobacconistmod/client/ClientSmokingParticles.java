package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.novelty.NoveltyContent;
import com.diggydwarff.tobacconistmod.novelty.client.NoveltyParticle;
import com.diggydwarff.tobacconistmod.util.MouthEffectProfile;
import com.diggydwarff.tobacconistmod.vapor.client.VaporParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosSmokingHelper;

public final class ClientSmokingParticles {
    private record State(MouthEffectProfile profile,int vegetable,long received) {}
    private static final java.util.Map<java.util.UUID,State> STATES = new java.util.HashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel stateLevel;
    private ClientSmokingParticles() {}
    public static void receive(com.diggydwarff.tobacconistmod.network.MouthEffectStatePayload payload) {
        var level=Minecraft.getInstance().level;
        if(level==null) return;
        if(level!=stateLevel) { STATES.clear();stateLevel=level; }
        if(payload.profile()<0 || payload.profile()>=MouthEffectProfile.values().length) return;
        if(payload.profile()==MouthEffectProfile.NONE.ordinal()) STATES.remove(payload.player());
        else STATES.put(payload.player(),new State(MouthEffectProfile.values()[payload.profile()],com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(payload.vegetable(),0,100),level.getGameTime()));
    }
    /** Called only when Curios is installed. Idle effects never spend liquid, charge or tobacco. */
    public static void tick(Minecraft mc) {
        if(mc.level!=stateLevel) { STATES.clear();stateLevel=mc.level; }
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        STATES.values().removeIf(state -> mc.level.getGameTime()-state.received()>60);
        int density = TobacconistConfig.CLIENT.particleDensity.get();
        boolean minimal = mc.options.particles().get() == ParticleStatus.MINIMAL;
        int[] budget = {64}; long now = mc.level.getGameTime();
        for (Player player : mc.level.players()) {
            if (budget[0] <= 0 || !player.isAlive() || player.isSpectator() || player.isInvisible()
                    || player.distanceToSqr(mc.player) > 64*64 || player.isUsingItem() || ClientDrawAnimations.isMouthDrawing(player)) continue;
            var visible = CuriosSmokingHelper.visible(player).stack();
            if(visible.isEmpty()) continue;
            var state = STATES.get(player.getUUID());
            // The currently equipped synced stack wins over a heartbeat for the previous item.
            var profile = MouthEffectProfile.of(visible);
            int puffStep = profile.idlePuffStep(now, player.getUUID(), density, minimal);
            if(puffStep < 0) continue;
            var blend = com.diggydwarff.tobacconistmod.vapor.VaporData.blend(visible);
            int vegetable = blend!=null ? blend.vegetablePercent() : state!=null && state.profile()==profile ? state.vegetable() : 0;
            Vec3 look = Vec3.directionFromRotation(player.getXRot(),player.getYHeadRot());
            Vec3 origin = player.getEyePosition().add(look.scale(profile==MouthEffectProfile.BUBBLES || profile==MouthEffectProfile.WIZARD || profile==MouthEffectProfile.SOUL || profile==MouthEffectProfile.ASTRAL || profile==MouthEffectProfile.CONFETTI || profile==MouthEffectProfile.SUN || profile==MouthEffectProfile.ECHO || profile==MouthEffectProfile.PETALS || profile==MouthEffectProfile.NOTES ? .46 : .32)).add(0,-.12,0);
            if (profile.smoke() && budget[0]-- > 0) com.diggydwarff.tobacconistmod.client.particle.TobaccoFanSmokeParticle.idle(mc.level,origin,look,profile==MouthEffectProfile.WIZARD ? .55F : 1F);
            switch (profile) {
                    case VAPOR -> { if (budget[0]-- > 0) VaporParticle.idle(mc.level,origin,look,vegetable); }
                    case BUBBLES -> { if (budget[0]-- > 0) com.diggydwarff.tobacconistmod.vapor.client.SoapBubbleParticle.idle(mc.level,origin,look); }
                    case CONFETTI -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.CONFETTI,player,origin,look,budget[0]);
                    case SOUL -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.SOUL,player,origin,look,budget[0]);
                    case ASTRAL -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.ASTRAL,player,origin,look,budget[0]);
                    case WIZARD -> { if (puffStep == 0) budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.WIZARD,player,origin,look,budget[0]); }
                    case PETALS -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.PETAL,player,origin,look,budget[0]);
                    case NOTES -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.WHISTLE,player,origin,look,budget[0]);
                    case SUN -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.SUN,player,origin,look,budget[0]);
                    case ECHO -> budget[0] -= NoveltyParticle.idle(NoveltyContent.Kind.ECHO,player,origin,look,budget[0]);
                    default -> {}
                }
        }
    }
}
