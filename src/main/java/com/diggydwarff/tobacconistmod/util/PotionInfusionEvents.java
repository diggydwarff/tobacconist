package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative, bounded courses. They are discarded on death, logout and dimension change. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class PotionInfusionEvents {
    private record Dose(String id, int amplifier, int ticks) {}
    private static final class Course {
        final List<Dose> effects;
        final int interval;
        int next, pulses = PotionInfusion.PULSES_PER_DRAW;
        Course(List<Dose> effects) {
            this.effects = effects;
            this.next = TobacconistConfig.SERVER.infusionDelayTicks.get();
            this.interval = Math.max(TobacconistConfig.SERVER.infusionPulseIntervalTicks.get(),
                    effects.stream().mapToInt(Dose::ticks).max().orElse(1) + 1);
        }
    }
    private static final Map<UUID, Course> COURSES = new HashMap<>();
    private PotionInfusionEvents() {}

    static boolean queue(Player player, CompoundTag infusion) {
        if (!(player instanceof ServerPlayer) || COURSES.containsKey(player.getUUID())) return false;
        List<Dose> effects = new ArrayList<>();
        var entries = infusion.getList("Effects", Tag.TAG_COMPOUND);
        if (entries.isEmpty() || entries.size() > 4) return false;
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.getCompound(i);
            String id = entry.getString("Effect");
            ResourceLocation key = ResourceLocation.tryParse(id);
            var holder = key == null ? java.util.Optional.<net.minecraft.core.Holder.Reference<net.minecraft.world.effect.MobEffect>>empty()
                    : BuiltInRegistries.MOB_EFFECT.getHolder(key);
            if (!PotionInfusion.allowed(id) || holder.isEmpty() || holder.get().value().isInstantenous()) return false;
            int ticks = Math.min(entry.getInt("PulseTicks"),
                    TobacconistConfig.SERVER.infusionMaxTotalTicks.get() / (PotionInfusion.DRAWS * PotionInfusion.PULSES_PER_DRAW));
            if (ticks < 1) return false;
            effects.add(new Dose(id, Math.clamp(entry.getInt("Amplifier"), 0,
                    TobacconistConfig.SERVER.infusionMaxAmplifier.get()), ticks));
        }
        COURSES.put(player.getUUID(), new Course(List.copyOf(effects)));
        return true;
    }

    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Course course = COURSES.get(player.getUUID());
        if (course == null) return;
        if (!PotionInfusion.enabled() || !player.isAlive() || player.isSpectator()) {
            COURSES.remove(player.getUUID());
            return;
        }
        if (--course.next > 0) return;
        for (Dose dose : course.effects) {
            if (!PotionInfusion.allowed(dose.id)) continue;
            BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(dose.id)).ifPresent(effect -> {
                // Never replace, amplify or refresh an existing potion/legendary effect.
                if (!player.hasEffect(effect)) player.addEffect(new MobEffectInstance(effect,
                        dose.ticks, dose.amplifier, false, true, true));
            });
        }
        if (--course.pulses <= 0) COURSES.remove(player.getUUID());
        else course.next = course.interval;
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        CompoundTag infusion = PotionInfusion.data(event.getItemStack());
        if (infusion.isEmpty()) return;
        event.getToolTip().add(Component.translatable("tobacconistmod.infusion.draws", Math.clamp(infusion.getInt("DrawsLeft"), 0, PotionInfusion.DRAWS))
                .withStyle(ChatFormatting.AQUA));
        var entries = infusion.getList("Effects", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(4, entries.size()); i++) {
            var entry = entries.getCompound(i);
            var id = ResourceLocation.tryParse(entry.getString("Effect"));
            if (id == null) continue;
            BuiltInRegistries.MOB_EFFECT.getHolder(id).ifPresent(effect -> event.getToolTip().add(
                    Component.translatable("tobacconistmod.infusion.effect", effect.value().getDisplayName(),
                            Math.max(0, entry.getInt("Amplifier")) + 1,
                            String.format(java.util.Locale.ROOT, "%.2f", Math.max(0, entry.getInt("PulseTicks")) / 20.0))
                            .withStyle(ChatFormatting.GRAY)));
        }
        event.getToolTip().add(Component.translatable("tobacconistmod.infusion.hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { COURSES.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void death(LivingDeathEvent event) { COURSES.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { COURSES.remove(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { COURSES.clear(); }
}
