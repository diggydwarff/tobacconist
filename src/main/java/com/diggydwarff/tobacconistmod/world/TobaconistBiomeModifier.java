package com.diggydwarff.tobacconistmod.world;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TobaconistBiomeModifier {
    private static final boolean ENABLED = true;

    /* Static registry objects */

    // Biome Modifier Serializers
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TobacconistMod.MODID);
    private static final RegistryObject<Codec<HerbalistModifier>> MODIFY_BIOMES = BIOME_MODIFIER_SERIALIZERS.register("modify_biomes", () ->
            RecordCodecBuilder.create(builder -> builder.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(HerbalistModifier::biomes),
                    Codec.STRING.xmap(s -> Biome.Precipitation.valueOf(s.toUpperCase(Locale.ROOT)), e -> e.name().toLowerCase(Locale.ROOT)).fieldOf("precipitation").forGetter(HerbalistModifier::precipitation),
                    Codec.INT.fieldOf("water_color").forGetter(HerbalistModifier::waterColor)
            ).apply(builder, HerbalistModifier::new))
    );

    public static void register(IEventBus modEventBus){
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
    }

    /* Dynamic registry objects */

    private static final ResourceKey<PlacedFeature> WILD_TOBACCO_PATCH = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(TobacconistMod.MODID, "wild_tobacco_patch"));
    //private static final ResourceKey<PlacedFeature> DIVINERS_SAGE_PATCH = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MODID, "diviners_sage_patch"));

    private static final ResourceKey<BiomeModifier> ADD_WILD_TOBACCO_PATCH = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(TobacconistMod.MODID, "add_wild_tobacco_patch"));
    //private static final ResourceKey<BiomeModifier> ADD_DIVINERS_SAGE_PATCH = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(MODID, "add_diviners_sage_patch"));


    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();

    private void onGatherData(GatherDataEvent event)
    {
        event.getGenerator().addProvider(event.includeServer(), (DataProvider.Factory<BiomeModifiers>) output -> new BiomeModifiers(output, event.getLookupProvider()));
    }

    private static class BiomeModifiers extends DatapackBuiltinEntriesProvider
    {

        public BiomeModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
        {
            super(output, registries, BUILDER, Set.of(TobacconistMod.MODID));
        }

        @Override
        public String getName()
        {
            return "Biome Modifier Registries: " + TobacconistMod.MODID;
        }
    }

    public record HerbalistModifier(HolderSet<Biome> biomes, Biome.Precipitation precipitation, int waterColor) implements BiomeModifier
    {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, Builder builder)
        {
            if (phase == Phase.MODIFY && this.biomes.contains(biome))
            {
                builder.getClimateSettings().setHasPrecipitation(true);
                builder.getSpecialEffects().waterColor(this.waterColor);
                if (this.precipitation == Biome.Precipitation.SNOW)
                    builder.getClimateSettings().setTemperature(0F);
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec()
        {
            return MODIFY_BIOMES.get();
        }
    }
}
