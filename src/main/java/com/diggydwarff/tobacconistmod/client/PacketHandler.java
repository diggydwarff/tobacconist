package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.ModList;

public class PacketHandler {
  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(TobacconistMod.MODID, "main"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals
  );

  public static void register() {
      int id = 0;

      if (ModList.get().isLoaded("curios")) {
        INSTANCE.registerMessage(id++, CurioUsePacket.class, CurioUsePacket::toBytes, CurioUsePacket::new, CurioUsePacket::handle);
      }
  }
}

