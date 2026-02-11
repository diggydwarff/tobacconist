package com.diggydwarff.tobacconistmod.villager;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.ModBlocks;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModVillagerSetup {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Holder<?> holder = ModVillagers.HOOKAH_POI.getHolder().orElseThrow();
                Set<BlockState> states = ImmutableSet.copyOf(
                        ModBlocks.HOOKAH.get().getStateDefinition().getPossibleStates()
                );

                Method m = ObfuscationReflectionHelper.findMethod(
                        PoiTypes.class,
                        "registerBlockStates",
                        Holder.class,
                        Set.class
                );
                m.setAccessible(true);
                m.invoke(null, holder, states);

            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
