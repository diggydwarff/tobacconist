package com.diggydwarff.tobacconistmod.compat.curios;

import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;

/** Registers the 1.20.1 Curios mouth/eyes slots through IMC. */
public final class MouthSlot {
    private MouthSlot() {}

    public static void register() {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> new SlotTypeMessage.Builder("mouth").size(1).priority(10).build());
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> new SlotTypeMessage.Builder("eyes").size(1).priority(11).build());
    }
}
