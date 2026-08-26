package com.diggydwarff.tobacconistmod.effect;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NicotineEffect extends MobEffect {

    private static final ResourceLocation NICOTINE_SPEED_ID =
            new ResourceLocation(TobacconistMod.MODID, "nicotine_speed");

    public NicotineEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                NICOTINE_SPEED_ID,
                0.05D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
