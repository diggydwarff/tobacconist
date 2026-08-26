package com.diggydwarff.tobacconistmod.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

/** Clear, low-viscosity rendering/physics shared by Aqua Vitae and flavoring essences. */
public final class ExtractionFluidType extends FluidType {
    private static final ResourceLocation STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    // Deliberately very translucent/pale: these are alcohol-like extracts, not fruit juice.
    private static final int AQUA_VITAE_TINT = 0x66FFF3D2;

    private final int tintColor;

    public ExtractionFluidType() {
        this(AQUA_VITAE_TINT);
    }

    public ExtractionFluidType(int tintColor) {
        super(FluidType.Properties.create().density(850).viscosity(900));
        this.tintColor = tintColor;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return tintColor;
            }
        });
    }
}
