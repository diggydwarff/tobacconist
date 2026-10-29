package com.diggydwarff.tobacconistmod.backport;

import net.minecraft.network.chat.Component;

/** Vanilla fields retain their native 1.20.1 NBT representations. */
public final class DataComponents {
    private DataComponents() {}
    public static final DataComponentType<Component> CUSTOM_NAME = DataComponentType.<Component>builder().build();
    public static final DataComponentType<DyedItemColor> DYED_COLOR = DataComponentType.<DyedItemColor>builder().build();
    public static final DataComponentType<PotionContents> POTION_CONTENTS = DataComponentType.<PotionContents>builder().build();
}
