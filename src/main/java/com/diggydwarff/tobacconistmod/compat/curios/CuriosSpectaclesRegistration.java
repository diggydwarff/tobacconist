package com.diggydwarff.tobacconistmod.compat.curios;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

/** Curios-only registration, loaded reflectively so Curios remains an optional dependency. */
public final class CuriosSpectaclesRegistration {
    private CuriosSpectaclesRegistration() {}

    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, CuriosSpectaclesRegistration::attach);
        modEventBus.addListener(CuriosSpectaclesRegistration::sendSlotTypes);
    }

    private static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!(stack.getItem() instanceof com.diggydwarff.tobacconistmod.datagen.items.custom.TobacconistsSpectaclesItem)
                && !CuriosSmokingHelper.supported(stack)) return;
        LazyOptional<ICurio> curio = LazyOptional.of(() -> () -> stack);
        event.addCapability(new ResourceLocation(TobacconistMod.MODID, "curio"), new ICapabilityProvider() {
            @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
                return CuriosCapability.ITEM.orEmpty(capability, curio);
            }
        });
        event.addListener(curio::invalidate);
    }

    private static void sendSlotTypes(net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent event) {
        net.minecraftforge.fml.InterModComms.sendTo("curios", top.theillusivec4.curios.api.SlotTypeMessage.REGISTER_TYPE,
                () -> top.theillusivec4.curios.api.SlotTypePreset.HEAD.getMessageBuilder().build());
        net.minecraftforge.fml.InterModComms.sendTo("curios", top.theillusivec4.curios.api.SlotTypeMessage.REGISTER_TYPE,
                () -> new top.theillusivec4.curios.api.SlotTypeMessage.Builder("mouth")
                        .size(1).build());
    }
}
