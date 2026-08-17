package com.diggydwarff.tobacconistmod;

import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.diggydwarff.tobacconistmod.block.entity.ModBlockEntities;
import com.diggydwarff.tobacconistmod.command.TobacconistCommands;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.compat.create.CreateCompat;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.effect.ModEffects;
import com.diggydwarff.tobacconistmod.recipes.ModRecipeSerializers;
import com.diggydwarff.tobacconistmod.recipes.ModRecipes;
import com.diggydwarff.tobacconistmod.screen.ModMenuTypes;
import com.diggydwarff.tobacconistmod.villager.ModVillagers;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(TobacconistMod.MODID)
public class TobacconistMod {
    public static final String MODID = "tobacconistmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TobacconistMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerDynamicItems);
        modEventBus.addListener(this::registerCapabilities);

        CreateCompat.init(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModVillagers.register(modEventBus);
        TobacconistCreativeTab.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, TobacconistConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, TobacconistConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, TobacconistConfig.SERVER_SPEC);

        NeoForge.EVENT_BUS.register(this);
    }

    private void registerDynamicItems(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            for (BottledMolassesFlavors flavor : BottledMolassesFlavors.values()) {
                // Plain molasses is already registered through ModItems.
                if (flavor == BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN) continue;
                helper.register(ResourceLocation.fromNamespaceAndPath(MODID, flavor.getName()), flavor.getItem());
            }
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.HOOKAH.get(),
                (hookah, side) -> hookah.getItemHandler()
        );
    }

    @SubscribeEvent
    public void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(
                Ingredient.of(Items.POTION),
                Ingredient.of(Items.SUGAR_CANE),
                new ItemStack(BottledMolassesFlavors.BOTTLED_MOLASSES_PLAIN.getItem())
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TobacconistCommands.register(event.getDispatcher());
    }
}
