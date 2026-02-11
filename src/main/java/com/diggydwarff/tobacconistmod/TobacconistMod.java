package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.recipes.ModRecipeSerializers;
import com.diggydwarff.tobacconistmod.villager.ModVillagerTrades;
import com.diggydwarff.tobacconistmod.world.TobaconistBiomeModifier;
import com.mojang.logging.LogUtils;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.datagen.items.custom.LooseTobacco;
import com.diggydwarff.tobacconistmod.recipes.ModRecipes;
import com.diggydwarff.tobacconistmod.screen.HookahScreen;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import com.diggydwarff.tobacconistmod.villager.ModVillagers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TobacconistMod.MODID)
public class TobacconistMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tobacconistmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public TobacconistMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        TobacconistCreativeTab.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::register);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModVillagers.register(modEventBus);
        ModRecipes.register(modEventBus);

        ModEffects.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        TobaconistBiomeModifier.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ModVillagerTrades.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS,
                helper -> {
                    for(BottledMolassesFlavors molassesFlavor : BottledMolassesFlavors.values()){
                        helper.register(new ResourceLocation(MODID, molassesFlavor.getName()), molassesFlavor.getItem());
                    }
                }
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        BrewingRecipeRegistry.addRecipe(Ingredient.of(Items.POTION), Ingredient.of(Items.SUGAR_CANE), new ItemStack(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WILD_TOBACCO_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VIRGINIA_TOBACCO_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BURLEY_TOBACCO_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ORIENTAL_TOBACCO_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DOKHA_TOBACCO_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHADE_TOBACCO_CROP.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HOOKAH.get(), RenderType.translucent());

            MenuScreens.register(ModMenuTypes.HOOKAH_MENU.get(), HookahScreen::new);
        }
    }
}
