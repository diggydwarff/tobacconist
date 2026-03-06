package com.diggydwarff.tobacconistmod.villager;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModVillagerTrades {
    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {

        var key = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(event.getType());
        if (!new ResourceLocation(TobacconistMod.MODID, "tobacconist_master").equals(key)) return;

        List<VillagerTrades.ItemListing> level1 = event.getTrades().get(1);
        if (ModItems.BURLEY_TOBACCO_SEEDS.isPresent()) {
            level1.add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.BURLEY_TOBACCO_SEEDS.get(), 1),
                    16, 1, 0.05f
            ));
        }

        if (ModItems.DOKHA_TOBACCO_SEEDS.isPresent()) {
            level1.add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.DOKHA_TOBACCO_SEEDS.get(), 1),
                    16, 1, 0.05f
            ));
        }

        if (ModItems.SHADE_TOBACCO_SEEDS.isPresent()) {
            level1.add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.SHADE_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        }

        if (ModItems.ORIENTAL_TOBACCO_SEEDS.isPresent()) {
            level1.add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.ORIENTAL_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        }

        if (ModItems.VIRGINIA_TOBACCO_SEEDS.isPresent()) {
            level1.add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.VIRGINIA_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        }

        if (ModItems.WILD_TOBACCO_SEEDS.isPresent()) {
            level1.add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.WILD_TOBACCO_SEEDS.get(), 1),
                    12, 1, 0.05f
            ));
        }
    }

}
