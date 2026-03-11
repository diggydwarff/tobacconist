package com.diggydwarff.tobacconistmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NicotineEffect extends MobEffect {

    private static final String NICOTINE_SPEED_UUID = "6f3d8d4e-0b8c-4f38-9f57-2a7d3b3d91a1";

    public NicotineEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                NICOTINE_SPEED_UUID,
                0.05D, // +5% move speed per level
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}