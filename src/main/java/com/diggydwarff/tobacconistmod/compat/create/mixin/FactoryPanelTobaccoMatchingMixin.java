package com.diggydwarff.tobacconistmod.compat.create.mixin;

import com.diggydwarff.tobacconistmod.compat.create.CreateFactoryGaugeTobaccoMatching;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/** Makes Factory Gauge restocking quality-tier-aware for Tobacconist items only. */
@Mixin(value = FactoryPanelBehaviour.class, remap = false)
public abstract class FactoryPanelTobaccoMatchingMixin {

    @Redirect(
            method = "tryRestock",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packagerLink/LogisticsManager;getStockOf(Ljava/util/UUID;Lnet/minecraft/world/item/ItemStack;Lcom/simibubi/create/content/logistics/packager/IdentifiedInventory;)I"
            )
    )
    private int tobacconist$tierMatchedNetworkStock(UUID network,
                                                     ItemStack requested,
                                                     @Nullable IdentifiedInventory ignoredInventory) {
        if (!CreateFactoryGaugeTobaccoMatching.supportsTierMatching(requested)) {
            return LogisticsManager.getStockOf(network, requested, ignoredInventory);
        }
        return CreateFactoryGaugeTobaccoMatching.getStockOf(network, requested, ignoredInventory);
    }

    @Redirect(
            method = "tryRestock",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packagerLink/LogisticsManager;broadcastPackageRequest(Ljava/util/UUID;Lcom/simibubi/create/content/logistics/packagerLink/LogisticallyLinkedBehaviour$RequestType;Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;Lcom/simibubi/create/content/logistics/packager/IdentifiedInventory;Ljava/lang/String;)Z"
            )
    )
    private boolean tobacconist$resolveTierMatchedRestockOrder(UUID network,
                                                                RequestType type,
                                                                PackageOrderWithCrafts order,
                                                                @Nullable IdentifiedInventory ignoredInventory,
                                                                String address) {
        PackageOrderWithCrafts resolved = CreateFactoryGaugeTobaccoMatching.resolveRestockOrder(
                network, order, ignoredInventory);
        return LogisticsManager.broadcastPackageRequest(network, type, resolved, ignoredInventory, address);
    }

    @Redirect(
            method = "getLevelInStorage",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int tobacconist$tierMatchedDestinationCount(InventorySummary summary, ItemStack requested) {
        return CreateFactoryGaugeTobaccoMatching.getCountOf(summary, requested);
    }
}
