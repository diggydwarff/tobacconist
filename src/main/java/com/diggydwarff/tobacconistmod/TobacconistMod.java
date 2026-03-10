package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.block.ModPaintings;
import com.diggydwarff.tobacconistmod.compat.curios.CuriosCompat;
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
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TobacconistMod.MODID)
public class TobacconistMod
{
    public static final String MODID = "tobacconistmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public TobacconistMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::register);
        modEventBus.addListener(this::enqueueIMC); // keep for IMC messages only

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        TobaconistBiomeModifier.register(modEventBus);
        ModVillagers.register(modEventBus);
        ModPaintings.PAINTING_VARIANTS.register(modEventBus);
        TobacconistCreativeTab.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.init(); // Only called here now
        }
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

        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(ModItems.SPOILED_TOBACCO.get(), 0.85f);
        });
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
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                var id = new ResourceLocation(TobacconistMod.MODID, "tobacco_crop_wild");

                // only do this if true, otherwise you'll crash again
                if (ForgeRegistries.BLOCKS.containsKey(id)) {
                    ItemBlockRenderTypes.setRenderLayer(ModBlocks.WILD_TOBACCO_CROP.get(), RenderType.cutout());
                }
            });
        }

    }
}
