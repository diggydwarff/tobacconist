package com.diggydwarff.tobacconistmod.villager;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

@EventBusSubscriber(modid = TobacconistMod.MODID)
public class ModVillagerTrades {
    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {

        var key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
        if (!ResourceLocation.fromNamespaceAndPath(TobacconistMod.MODID, "tobacconist_master").equals(key)) return;

        List<VillagerTrades.ItemListing> level1 = event.getTrades().get(1);
        level1.add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.BURLEY_TOBACCO_SEEDS.get(), 1),
                    16, 1, 0.05f
            ));
        level1.add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.DOKHA_TOBACCO_SEEDS.get(), 1),
                    16, 1, 0.05f
            ));
        level1.add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.SHADE_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        level1.add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.ORIENTAL_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        level1.add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.VIRGINIA_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        level1.add((trader, rand) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(ModItems.WILD_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
    }

}
