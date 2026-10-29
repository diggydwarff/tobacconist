package com.diggydwarff.tobacconistmod.novelty;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.mojang.serialization.Codec;
import com.diggydwarff.tobacconistmod.backport.DataComponentType;
import net.minecraft.core.registries.Registries;
import com.diggydwarff.tobacconistmod.backport.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.function.Supplier;

public final class NoveltyContent {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TobacconistMod.MODID);
    private static final com.diggydwarff.tobacconistmod.backport.DataKeys DATA = com.diggydwarff.tobacconistmod.backport.DataKeys.create(TobacconistMod.MODID);
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TobacconistMod.MODID);
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, TobacconistMod.MODID);
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> CONFETTI_PARTICLE = particle("confetti_piece");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> RING_PARTICLE = particle("relic_ring");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> SOUL_PARTICLE = particle("relic_soul");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> SOUL_MOTE_PARTICLE = particle("soulglass_glint");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> STAR_PARTICLE = particle("astral_star");
    public static final Supplier<DataComponentType<Integer>> CHARGES = DATA.register("novelty_pipe_charge",
            () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.intRange(0, 24)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<Item> CONFETTI_PIPE = ITEMS.register("confetti_pipe", () -> new ToyPipeItem(new Item.Properties().stacksTo(1), Kind.CONFETTI));
    public static final Supplier<Item> WIZARDS_LONGPIPE = ITEMS.register("wizards_longpipe", () -> new RelicPipeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), Kind.WIZARD));
    public static final Supplier<Item> SOULGLASS_PIPE = ITEMS.register("soulglass_pipe", () -> new RelicPipeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), Kind.SOUL));
    public static final Supplier<Item> ASTRAL_PIPE = ITEMS.register("astral_pipe", () -> new RelicPipeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), Kind.ASTRAL));
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> PETAL_PARTICLE = particle("pipe_petal");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> NOTE_PARTICLE = particle("pipe_note");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> SUN_PARTICLE = particle("sunkeeper_smoke");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> ECHO_PARTICLE = particle("echo_pulse");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> SOUL_EMBER_PARTICLE = particle("soulglass_ember");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> SUN_GLEAM_PARTICLE = particle("sunkeeper_gleam");
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> ECHO_MOTE_PARTICLE = particle("echo_mote");
    public static final Supplier<Item> PETAL_PIPE = ITEMS.register("petal_pipe", () -> new ToyPipeItem(new Item.Properties().stacksTo(1), Kind.PETAL));
    public static final Supplier<Item> WHISTLING_PIPE = ITEMS.register("whistling_pipe", () -> new ToyPipeItem(new Item.Properties().stacksTo(1), Kind.WHISTLE));
    public static final Supplier<Item> SUNKEEPERS_PIPE = ITEMS.register("sunkeepers_pipe", () -> new RelicPipeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), Kind.SUN));
    public static final Supplier<Item> ECHO_PIPE = ITEMS.register("echo_pipe", () -> new RelicPipeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), Kind.ECHO));
    static { LOOT.register("relic_pipes", () -> RelicLootModifier.CODEC); }
    public enum Kind { CONFETTI, WIZARD, SOUL, ASTRAL, PETAL, WHISTLE, SUN, ECHO }
    private NoveltyContent() {}
    private static Supplier<net.minecraft.core.particles.SimpleParticleType> particle(String name) { return PARTICLES.register(name, () -> new net.minecraft.core.particles.SimpleParticleType(false)); }
    public static void register(IEventBus bus) { ITEMS.register(bus); DATA.register(bus); LOOT.register(bus); PARTICLES.register(bus); }
}
