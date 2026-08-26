package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/** Registers Tobacconist metadata with Create's Attribute Filter registry. */
public final class CreateItemAttributeCompat {
    private static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTES =
            DeferredRegister.create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, TobacconistMod.MODID);

    public static final RegistryObject<CreateTobaccoItemAttribute.Type> TOBACCO_METADATA =
            ITEM_ATTRIBUTES.register("tobacco_metadata", CreateTobaccoItemAttribute.Type::new);

    private CreateItemAttributeCompat() {}

    public static void register(IEventBus modEventBus) {
        ITEM_ATTRIBUTES.register(modEventBus);
        TobacconistMod.LOGGER.info("Create Attribute Filter tobacco metadata integration enabled.");
    }
}
