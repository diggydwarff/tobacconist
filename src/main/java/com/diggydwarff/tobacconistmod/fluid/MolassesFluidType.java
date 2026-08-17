package com.diggydwarff.tobacconistmod.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

/** Shared physical/rendering behavior for Tobacconist molasses fluids. */
public final class MolassesFluidType extends FluidType {
    private static final ResourceLocation STILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tobacconistmod", "block/molasses_still");
    private static final ResourceLocation FLOWING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tobacconistmod", "block/molasses_flow");
    private static final int MOLASSES_TINT = 0xFF160A04;

    public MolassesFluidType() {
        super(FluidType.Properties.create()
                .density(1400)
                .viscosity(3000));
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
                return MOLASSES_TINT;
            }
        });
    }
}
