package com.diggydwarff.tobacconistmod.compat.curios;

import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;

public class MouthSlot {

    public static void register() {
        InterModComms.sendTo(
                CuriosApi.MODID,
                SlotTypeMessage.REGISTER_TYPE,
                () -> new SlotTypeMessage.Builder("mouth")
                        .size(1)
                        .priority(10)
                        .build()
        );
    }
}