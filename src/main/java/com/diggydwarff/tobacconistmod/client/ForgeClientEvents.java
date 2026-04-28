package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
      if (ClientSetup.USE_AS_CURIO.consumeClick()) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, 
            stack -> stack.getItem() instanceof SmokingItem)
            .ifPresent(slotResult -> {
              PacketHandler.INSTANCE.sendToServer(new CurioUsePacket());
            });
      }
    }
}
