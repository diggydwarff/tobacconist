package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class ModEvents {
    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if(event.getType() == VillagerProfession.TOOLSMITH) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            ItemStack stack = new ItemStack(ModItems.VIRGINIA_TOBACCO_SEEDS.get(), 1);
            int villagerLevel = 1;

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    stack,10,8,0.02F));
        }

        if(event.getType() == VillagerProfession.FARMER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            ItemStack VIRGINIA_TOBACCO_SEEDS = new ItemStack(ModItems.VIRGINIA_TOBACCO_SEEDS.get(), 1);
            ItemStack BURLEY_TOBACCO_SEEDS = new ItemStack(ModItems.BURLEY_TOBACCO_SEEDS.get(), 1);
            ItemStack ORIENTAL_TOBACCO_SEEDS = new ItemStack(ModItems.ORIENTAL_TOBACCO_SEEDS.get(), 1);
            ItemStack WILD_TOBACCO_SEEDS = new ItemStack(ModItems.WILD_TOBACCO_SEEDS.get(), 1);
            ItemStack DOKHA_TOBACCO_SEEDS = new ItemStack(ModItems.DOKHA_TOBACCO_SEEDS.get(), 1);
            ItemStack SHADE_TOBACCO_SEEDS = new ItemStack(ModItems.SHADE_TOBACCO_SEEDS.get(), 1);
            int villagerLevel = 1;

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    VIRGINIA_TOBACCO_SEEDS,10,8,0.02F));

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    BURLEY_TOBACCO_SEEDS,10,8,0.02F));

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    ORIENTAL_TOBACCO_SEEDS,10,8,0.02F));

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    WILD_TOBACCO_SEEDS,10,8,0.02F));

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    DOKHA_TOBACCO_SEEDS,10,8,0.02F));

            trades.get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    SHADE_TOBACCO_SEEDS,10,8,0.02F));
        }
    }
}
