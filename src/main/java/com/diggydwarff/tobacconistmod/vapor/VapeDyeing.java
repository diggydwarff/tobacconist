package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Fresh-color armor-dye crafting preserves device data; water removes only the dye. */
public final class VapeDyeing {
    private VapeDyeing() {}
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CauldronInteraction.WATER.map().put(VaporContent.VAPE_PEN.get(), CauldronInteraction.DYED_ITEM);
            CauldronInteraction.WATER.map().put(VaporContent.BOX_MOD.get(), CauldronInteraction.DYED_ITEM);
            CauldronInteraction.WATER.map().put(VaporContent.CIGALIKE.get(), CauldronInteraction.DYED_ITEM);
        });
    }
}
