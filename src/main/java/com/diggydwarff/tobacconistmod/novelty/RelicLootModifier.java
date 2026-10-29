package com.diggydwarff.tobacconistmod.novelty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/** Adds at most one pipe and preserves all normal chest loot. */
public final class RelicLootModifier extends LootModifier {
    public static final Codec<RelicLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, RelicLootModifier::new));
    public RelicLootModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!NoveltyConfig.RELIC_LOOT.get()) return loot;
        String table = context.getQueriedLootTableId().toString();
        Item item; double chance;
        switch (table) {
            case "minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft" -> { item = NoveltyContent.WIZARDS_LONGPIPE.get(); chance = NoveltyConfig.RARE_CHANCE.get(); }
            case "minecraft:chests/nether_bridge", "minecraft:chests/bastion_treasure" -> { item = NoveltyContent.SOULGLASS_PIPE.get(); chance = NoveltyConfig.MYTHIC_CHANCE.get(); }
            case "minecraft:chests/end_city_treasure" -> { item = NoveltyContent.ASTRAL_PIPE.get(); chance = NoveltyConfig.MYTHIC_CHANCE.get(); }
            case "minecraft:chests/desert_pyramid" -> { item = NoveltyContent.SUNKEEPERS_PIPE.get(); chance = NoveltyConfig.RARE_CHANCE.get(); }
            case "minecraft:chests/ancient_city" -> { item = NoveltyContent.ECHO_PIPE.get(); chance = NoveltyConfig.MYTHIC_CHANCE.get(); }
            default -> { return loot; }
        }
        if (context.getRandom().nextDouble() < chance) loot.add(new ItemStack(item));
        return loot;
    }
    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
