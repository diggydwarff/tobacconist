package com.diggydwarff.tobacconistmod.novelty;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.util.SmokingDraws;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import com.diggydwarff.tobacconistmod.backport.PacketDistributor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class NoveltyEffects {
    private record Pending(NoveltyContent.Kind kind, int draw, int delay, Level level) {}
    private record Melody(List<WhistleMelody.Note> notes, int next, int age, Level level) {}
    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, Melody> MELODIES = new HashMap<>();

    private NoveltyEffects() {}

    public static void queue(ServerPlayer player, NoveltyContent.Kind kind, int draw) {
        if (!TobacconistConfig.areSmokingDrawAnimationsEnabled()) { emit(player, kind, draw, 0); return; }
        PENDING.put(player.getUUID(), new Pending(kind, com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw, 1, 30), SmokingDraws.EXHALE_DELAY_TICKS, player.level()));
    }

    public static void emit(ServerPlayer player, NoveltyContent.Kind kind, int draw, int color) {
        draw=com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw,1,30);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new NoveltyPuffPayload(player.getUUID(), kind.ordinal(), draw, color & 0xFFFFFF));
        if (!NoveltyConfig.SOUNDS.get()) return;
        if (kind==NoveltyContent.Kind.WHISTLE) {
            var notes=WhistleMelody.notes(draw,player.getRandom().nextInt(2));
            whistle(player,notes.get(0).pitch());
            if(notes.size()>1) MELODIES.put(player.getUUID(),new Melody(notes,1,0,player.level()));
            return;
        }
        net.minecraft.sounds.SoundEvent sound = switch (kind) {
            case CONFETTI -> SoundEvents.BOOK_PAGE_TURN;
            case ASTRAL -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case SOUL -> SoundEvents.SOUL_ESCAPE;
            case PETAL -> SoundEvents.CHERRY_LEAVES_STEP;
            case ECHO -> SoundEvents.NOTE_BLOCK_CHIME.value();
            default -> null;
        };
        if (sound != null) player.serverLevel().playSound(null, player.blockPosition(), sound,
                SoundSource.PLAYERS, kind==NoveltyContent.Kind.ECHO ? .18F : .15F, 1.1F);
    }

    private static void whistle(ServerPlayer player,float pitch) {
        player.serverLevel().playSound(null,player.getX(),player.getY()+1.4,player.getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.PLAYERS,.65F,pitch);
    }

    @SubscribeEvent public static void tick(PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        UUID id=player.getUUID();
        var pending = PENDING.get(id);
        if (pending != null) {
            if (!player.isAlive() || player.isSpectator() || player.level() != pending.level) PENDING.remove(id);
            else if (pending.delay <= 1) { PENDING.remove(id); emit(player, pending.kind, pending.draw, 0); }
            else PENDING.put(id, new Pending(pending.kind, pending.draw, pending.delay - 1, pending.level));
        }
        var melody=MELODIES.get(id);
        if(melody==null) return;
        if(!player.isAlive() || player.isSpectator() || player.level()!=melody.level || !NoveltyConfig.SOUNDS.get()) {
            MELODIES.remove(id);return;
        }
        int age=melody.age+1,next=melody.next;
        if(age>=melody.notes.get(next).delay()) whistle(player,melody.notes.get(next++).pitch());
        if(next>=melody.notes.size()) MELODIES.remove(id);
        else MELODIES.put(id,new Melody(melody.notes,next,age,melody.level));
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id=event.getEntity().getUUID();PENDING.remove(id);MELODIES.remove(id);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { PENDING.clear();MELODIES.clear(); }
}
