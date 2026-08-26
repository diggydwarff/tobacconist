package com.diggydwarff.tobacconistmod.event;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.block.entity.FlueFireboxBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.HookahEntity;
import com.diggydwarff.tobacconistmod.block.entity.IndustrialDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.block.entity.TobaccoDryingRackBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.BottledMolassesFlavors;
import com.diggydwarff.tobacconistmod.fluid.AquaVitaeBottleFluidHandler;
import com.diggydwarff.tobacconistmod.fluid.EssenceBottleFluidHandler;
import com.diggydwarff.tobacconistmod.fluid.GlassBottleMolassesFluidHandler;
import com.diggydwarff.tobacconistmod.fluid.MolassesBottleFluidHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** Forge 1.20.1 capability bridge for the NeoForge capability registrations used by 1.21.1. */
@Mod.EventBusSubscriber(modid = TobacconistMod.MODID)
public final class CapabilityEvents {
    private CapabilityEvents() {}

    @SubscribeEvent
    public static void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();
        if (be instanceof HookahEntity hookah) {
            attachItemHandler(event, side -> hookah.getItemHandler(), "hookah_items");
        } else if (be instanceof IndustrialDryingRackBlockEntity rack) {
            attachItemHandler(event, rack::getItemHandler, "industrial_drying_rack_items");
        } else if (be instanceof TobaccoDryingRackBlockEntity rack) {
            attachItemHandler(event, rack::getItemHandler, "drying_rack_items");
        } else if (be instanceof TobaccoBarrelBlockEntity barrel) {
            attachItemHandler(event, barrel::getItemHandler, "barrel_items");
        } else if (be instanceof FlueFireboxBlockEntity firebox) {
            attachItemHandler(event, firebox::getItemHandler, "flue_firebox_items");
        }
    }

    @SubscribeEvent
    public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        BottledMolassesFlavors molasses = BottledMolassesFlavors.fromItem(stack.getItem());
        if (molasses != null) {
            attachFluidHandler(event, () -> new MolassesBottleFluidHandler(stack, molasses), "molasses_bottle");
            return;
        }

        BottledMolassesFlavors essence = BottledMolassesFlavors.fromEssenceItem(stack.getItem());
        if (essence != null) {
            attachFluidHandler(event, () -> new EssenceBottleFluidHandler(stack, essence), "essence_bottle");
            return;
        }

        if (stack.is(ModItems.BOTTLED_AQUA_VITAE.get())) {
            attachFluidHandler(event, () -> new AquaVitaeBottleFluidHandler(stack), "aqua_vitae_bottle");
        } else if (stack.is(Items.GLASS_BOTTLE)) {
            attachFluidHandler(event, () -> new GlassBottleMolassesFluidHandler(stack), "processing_bottle");
        }
    }

    private static void attachItemHandler(AttachCapabilitiesEvent<BlockEntity> event,
                                          Function<Direction, IItemHandler> factory,
                                          String name) {
        DirectionalItemProvider provider = new DirectionalItemProvider(factory);
        event.addCapability(new ResourceLocation(TobacconistMod.MODID, name), provider);
        event.addListener(provider::invalidate);
    }

    private static void attachFluidHandler(AttachCapabilitiesEvent<ItemStack> event,
                                           Supplier<IFluidHandlerItem> factory,
                                           String name) {
        FluidItemProvider provider = new FluidItemProvider(factory);
        event.addCapability(new ResourceLocation(TobacconistMod.MODID, name), provider);
        event.addListener(provider::invalidate);
    }

    private static final class DirectionalItemProvider implements ICapabilityProvider {
        private final Function<Direction, IItemHandler> factory;
        private final Map<Direction, LazyOptional<IItemHandler>> sided = new EnumMap<>(Direction.class);
        private LazyOptional<IItemHandler> unsided;

        private DirectionalItemProvider(Function<Direction, IItemHandler> factory) {
            this.factory = factory;
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap != ForgeCapabilities.ITEM_HANDLER) return LazyOptional.empty();
            LazyOptional<IItemHandler> optional;
            if (side == null) {
                if (unsided == null) unsided = LazyOptional.of(() -> factory.apply(null));
                optional = unsided;
            } else {
                optional = sided.computeIfAbsent(side, direction -> LazyOptional.of(() -> factory.apply(direction)));
            }
            return optional.cast();
        }

        private void invalidate() {
            if (unsided != null) unsided.invalidate();
            sided.values().forEach(LazyOptional::invalidate);
            sided.clear();
        }
    }

    private static final class FluidItemProvider implements ICapabilityProvider {
        private final LazyOptional<IFluidHandlerItem> optional;

        private FluidItemProvider(Supplier<IFluidHandlerItem> factory) {
            this.optional = LazyOptional.of(() -> factory.get());
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.FLUID_HANDLER_ITEM ? optional.cast() : LazyOptional.empty();
        }

        private void invalidate() {
            optional.invalidate();
        }
    }
}
