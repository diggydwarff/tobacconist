package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabContents {

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TobacconistCreativeTab.COURSE_TAB.getKey()) {
            event.accept(makePipe(Items.OAK_PLANKS));
            event.accept(makePipe(Items.BIRCH_PLANKS));
            event.accept(makePipe(Items.ACACIA_PLANKS));
            event.accept(makePipe(Items.BAMBOO_PLANKS));
            event.accept(makePipe(Items.CHERRY_PLANKS));
            event.accept(makePipe(Items.CRIMSON_PLANKS));
            event.accept(makePipe(Items.DARK_OAK_PLANKS));
            event.accept(makePipe(Items.JUNGLE_PLANKS));
            event.accept(makePipe(Items.MANGROVE_PLANKS));
            event.accept(makePipe(Items.SPRUCE_PLANKS));
            event.accept(makePipe(Items.WARPED_PLANKS));
        }
    }

    private static ItemStack makePipe(Item plankItem) {
        ItemStack pipe = new ItemStack(ModItems.WOODEN_SMOKING_PIPE.get());
        pipe.getOrCreateTag().putString(
                WoodenPipeRecipe.NBT_WOOD_PLANK,
                BuiltInRegistries.ITEM.getKey(plankItem).toString()
        );
        return pipe;
    }
}