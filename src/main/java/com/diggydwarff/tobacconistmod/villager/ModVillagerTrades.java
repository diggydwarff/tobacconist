package com.diggydwarff.tobacconistmod.villager;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = TobacconistMod.MODID)
public class ModVillagerTrades {

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {
        var key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
        if (!ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "tobacconist_master").equals(key)) return;

        // Two novice offers means both regional seed varieties are guaranteed on a normal Tobacconist.
        // Wild seed is intentionally excluded: find it naturally or from a Wandering Trader.
        List<VillagerTrades.ItemListing> level1 = event.getTrades().get(1);
        level1.add(regionalSeedTrade(false));
        level1.add(regionalSeedTrade(true));

        List<VillagerTrades.ItemListing> level2 = event.getTrades().get(2);
        level2.add(regionalRawLeafPurchase());
        level2.add(sell(ModItems.ROLLING_PAPER, 1, 8, 12, 5));

        List<VillagerTrades.ItemListing> level3 = event.getTrades().get(3);
        level3.add(sell(ModItems.CLAY_SMOKING_PIPE, 4, 1, 8, 10));
        level3.add(sell(ModItems.TOBACCO_POUCH, 5, 1, 8, 10));

        List<VillagerTrades.ItemListing> level4 = event.getTrades().get(4);
        level4.add(sell(ModItems.BAMBOO_CHARCOAL, 2, 8, 12, 15));
        level4.add(sell(ModItems.BOTTLED_MOLASSES_PLAIN, 4, 1, 8, 15));

        List<VillagerTrades.ItemListing> level5 = event.getTrades().get(5);
        level5.add(sell(ModItems.TOBACCO_BOX, 8, 1, 4, 30));
        level5.add(sell(ModItems.TOBACCONISTS_SPECTACLES, 12, 1, 2, 30));
    }

    @SubscribeEvent
    public static void addWanderingTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(sell(ModItems.WILD_TOBACCO_SEEDS, 1, 2, 8, 1));
        event.getRareTrades().add((trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 3),
                new ItemStack(com.diggydwarff.tobacconistmod.block.ModBlocks.WILD_FLOWERING_TOBACCO.get(), 1),
                4, 1, 0.05F
        ));
    }

    private static VillagerTrades.ItemListing regionalSeedTrade(boolean secondary) {
        return (trader, random) -> {
            RegionalProfile profile = profileFor(trader);
            Item seed = (secondary ? profile.secondarySeed() : profile.primarySeed()).get();
            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(seed, 1),
                    16, 1, 0.05F
            );
        };
    }

    private static VillagerTrades.ItemListing regionalRawLeafPurchase() {
        return (trader, random) -> {
            Item leaf = profileFor(trader).primaryRawLeaf().get();
            return new MerchantOffer(
                    new ItemCost(leaf, 12),
                    new ItemStack(Items.EMERALD, 1),
                    12, 5, 0.05F
            );
        };
    }

    private static VillagerTrades.ItemListing sell(Supplier<Item> item, int emeraldCost, int count, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCost),
                new ItemStack(item.get(), count),
                maxUses, xp, 0.05F
        );
    }

    private static RegionalProfile profileFor(Entity trader) {
        Holder<Biome> biome = trader.level().getBiome(trader.blockPosition());

        // The order intentionally handles the strongest specialty regions first.
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            return new RegionalProfile(ModItems.ORIENTAL_TOBACCO_SEEDS, ModItems.DOKHA_TOBACCO_SEEDS,
                    ModItems.ORIENTAL_TOBACCO_LEAF);
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_DESERT)) {
            return new RegionalProfile(ModItems.DOKHA_TOBACCO_SEEDS, ModItems.ORIENTAL_TOBACCO_SEEDS,
                    ModItems.DOKHA_TOBACCO_LEAF);
        }
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            return new RegionalProfile(ModItems.SHADE_TOBACCO_SEEDS, ModItems.BURLEY_TOBACCO_SEEDS,
                    ModItems.SHADE_TOBACCO_LEAF);
        }
        if (biome.is(BiomeTags.IS_FOREST)) {
            return new RegionalProfile(ModItems.BURLEY_TOBACCO_SEEDS, ModItems.SHADE_TOBACCO_SEEDS,
                    ModItems.BURLEY_TOBACCO_LEAF);
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_SAVANNA) || biome.is(BiomeTags.IS_SAVANNA)) {
            return new RegionalProfile(ModItems.VIRGINIA_TOBACCO_SEEDS, ModItems.ORIENTAL_TOBACCO_SEEDS,
                    ModItems.VIRGINIA_TOBACCO_LEAF);
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_SNOWY)) {
            return new RegionalProfile(ModItems.BURLEY_TOBACCO_SEEDS, ModItems.VIRGINIA_TOBACCO_SEEDS,
                    ModItems.BURLEY_TOBACCO_LEAF);
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_TAIGA) || biome.is(BiomeTags.IS_TAIGA)) {
            return new RegionalProfile(ModItems.BURLEY_TOBACCO_SEEDS, ModItems.SHADE_TOBACCO_SEEDS,
                    ModItems.BURLEY_TOBACCO_LEAF);
        }

        // Plains villages and modded/unknown biomes get the broad temperate pair.
        return new RegionalProfile(ModItems.VIRGINIA_TOBACCO_SEEDS, ModItems.BURLEY_TOBACCO_SEEDS,
                ModItems.VIRGINIA_TOBACCO_LEAF);
    }

    private record RegionalProfile(Supplier<Item> primarySeed, Supplier<Item> secondarySeed,
                                   Supplier<Item> primaryRawLeaf) {}
}
