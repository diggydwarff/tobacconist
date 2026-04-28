package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public class CurioUsePacket {
  public CurioUsePacket() {}

  public CurioUsePacket(FriendlyByteBuf buf) {}

  public void toBytes(FriendlyByteBuf buf) {}

  public boolean handle(Supplier<NetworkEvent.Context> supplier) {
    NetworkEvent.Context context = supplier.get();
    context.enqueueWork(() -> {
      ServerPlayer player = context.getSender();
      if (player != null) {
        CuriosApi.getCuriosHelper().findFirstCurio(player, 
            stack -> stack.getItem() instanceof SmokingItem)
            .ifPresent(slotResult -> {
              ItemStack stack = slotResult.stack();
              SmokingItem item = (SmokingItem)stack.getItem();
              item.performSmoke(player.level(), player, stack, p -> {
                  CuriosApi.getCuriosHelper()
                    .onBrokenCurio(slotResult.slotContext());
              });
            });
      } 
    });
    return true;
  }
}
