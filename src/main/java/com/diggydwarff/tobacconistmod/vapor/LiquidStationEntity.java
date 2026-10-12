package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.fluid.ModExtractionFluids;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import java.util.*;

/** The extraction half is generic; the blending half measures from six independent reservoirs. */
public final class LiquidStationEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT = 7, CARD = 8, FUEL = 9, GLASS = 10, SLOT_COUNT = 11;
    private int progress, burn, vegetablePercent = 70, strength, duration = 200;
    private boolean automatic;
    private ResourceLocation activeRecipe;
    private LiquidBlend activeBlend;
    public final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == OUTPUT || slot == GLASS || validInput(slot, stack); }
    };
    public final FluidTank[] tanks = new FluidTank[6];
    public final ContainerData data = new ContainerData() {
        @Override public int getCount() { return 13; }
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress; case 1 -> burn; case 2 -> vegetablePercent; case 3 -> strength;
                case 12 -> duration; case 4 -> automatic ? 1 : 0; case 5 -> isBlending() ? 1 : 0;
                default -> index >= 6 && index < 12 ? tanks[index - 6].getFluidAmount() : 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) { case 0 -> progress = value; case 1 -> burn = value; case 2 -> vegetablePercent = value;
                case 3 -> strength = value; case 4 -> automatic = value != 0; }
        }
    };

    public LiquidStationEntity(BlockPos pos, BlockState state) {
        super(VaporContent.STATION.get(), pos, state);
        for (int i = 0; i < tanks.length; i++) {
            final int slot = i;
            tanks[i] = new FluidTank(2000, fluid -> validFluid(slot, fluid)) {
                @Override protected void onContentsChanged() { setChanged(); }
            };
        }
    }
    public boolean isBlending() { return getBlockState().is(VaporContent.BLENDING_BENCH.get()); }

    public boolean validInput(int slot, ItemStack stack) {
        if (slot == OUTPUT || slot == GLASS) return false;
        if (slot == FUEL) return !isBlending() && stack.getBurnTime(RecipeType.SMELTING) > 0;
        if (slot == CARD) return isBlending() && stack.is(VaporContent.RECIPE_CARD.get());
        if (!isBlending()) return slot < 3;
        if (slot == 6) return stack.is(Items.GLASS_BOTTLE);
        if (slot >= 0 && slot < 6) {
            var handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            return handler != null && validFluid(slot, handler.getFluidInTank(0));
        }
        return false;
    }
    private boolean validFluid(int slot, FluidStack fluid) {
        if (fluid.isEmpty()) return false;
        return switch (slot) {
            case 0 -> fluid.getFluid() == VaporContent.VEGETABLE_FLUID.get();
            case 1 -> fluid.getFluid() == VaporContent.CLEAR_FLUID.get();
            case 2 -> fluid.getFluid() == VaporContent.NICOTINE_FLUID.get();
            default -> flavor(fluid) != null;
        };
    }

    public IItemHandler handler(Direction side) {
        // Outputs and returned bottles leave through the bottom. Inputs never leak mid-process.
        int[] slots = side == Direction.DOWN ? new int[]{OUTPUT, GLASS} : isBlending()
                ? new int[]{0, 1, 2, 3, 4, 5, 6} : new int[]{0, 1, 2, FUEL};
        return new IItemHandler() {
            @Override public int getSlots() { return slots.length; }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slots[slot]); }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slots[slot]); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return side != Direction.DOWN && inventory.isItemValid(slots[slot], stack); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return side == Direction.DOWN ? stack : inventory.insertItem(slots[slot], stack, simulate);
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return side == Direction.DOWN ? inventory.extractItem(slots[slot], amount, simulate) : ItemStack.EMPTY;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LiquidStationEntity station) {
        if (station.isBlending()) station.tickBlend(); else station.tickExtraction();
        if (level.getGameTime() % 20 == 0 && station.isBlending()) level.sendBlockUpdated(pos, state, state, 2);
    }
    private void tickExtraction() {
        var input = new ExtractionRecipe.Contents(List.of(inventory.getStackInSlot(0), inventory.getStackInSlot(1), inventory.getStackInSlot(2)));
        var match = level.getRecipeManager().getRecipeFor(VaporContent.EXTRACTION.get(), input, level);
        if (match.isEmpty() || !inventory.insertItem(OUTPUT, match.get().value().result().copy(), true).isEmpty()) {
            progress = 0; activeRecipe = null; return;
        }
        var holder = match.get(); var recipe = holder.value(); duration = recipe.duration();
        if (!holder.id().equals(activeRecipe)) { progress = 0; activeRecipe = holder.id(); }
        if (burn <= 0) {
            ItemStack fuel = inventory.getStackInSlot(FUEL);
            int time = fuel.getBurnTime(RecipeType.SMELTING);
            if (time <= 0) return;
            ItemStack remainder = fuel.getCraftingRemainingItem();
            if (!remainder.isEmpty() && !inventory.insertItem(GLASS, remainder, true).isEmpty()) return;
            burn = time; inventory.extractItem(FUEL, 1, false);
            if (!remainder.isEmpty()) inventory.insertItem(GLASS, remainder, false);
        }
        burn--; progress++;
        if (progress >= recipe.duration()) {
            for (int i = 0; i < recipe.inputs().size(); i++) inventory.extractItem(i, recipe.inputs().get(i).count(), false);
            inventory.insertItem(OUTPUT, recipe.result().copy(), false);
            progress = 0;
        }
        setChanged();
    }

    private void tickBlend() {
        for (int i = 0; i < 6; i++) loadBottle(i);
        if (!automatic) return;
        LiquidBlend desired = desiredBlend();
        if (desired == null || !canBlend(desired)) { progress = 0; activeBlend = null; return; }
        if (!desired.equals(activeBlend)) { activeBlend = desired; progress = 0; }
        if (++progress >= 80) { blend(desired); progress = 0; }
        setChanged();
    }
    private void loadBottle(int slot) {
        ItemStack input = inventory.getStackInSlot(slot);
        if (input.isEmpty()) return;
        var handler = input.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return;
        FluidStack fluid = handler.getFluidInTank(0);
        if (!validFluid(slot, fluid) || fluid.getAmount() <= 0) return;
        if (tanks[slot].fill(fluid, IFluidHandler.FluidAction.SIMULATE) != fluid.getAmount()) return;
        if (!inventory.insertItem(GLASS, new ItemStack(Items.GLASS_BOTTLE), true).isEmpty()) return;
        FluidStack drained = handler.drain(fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;
        tanks[slot].fill(drained, IFluidHandler.FluidAction.EXECUTE);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        inventory.insertItem(GLASS, handler.getContainer(), false);
    }
    public LiquidBlend desiredBlend() {
        LiquidBlend saved = inventory.getStackInSlot(CARD).get(VaporContent.BLEND.get());
        if (saved != null) return saved.usable() ? saved : null;
        Map<String, Integer> flavors = new TreeMap<>();
        for (int i = 3; i < 6; i++) {
            String flavor = flavor(tanks[i].getFluid());
            if (flavor != null) flavors.merge(flavor, 10, Integer::sum);
        }
        int nic = strength * 5;
        int carrier = 250 - nic - flavors.values().stream().mapToInt(Integer::intValue).sum();
        int vg = (int) Math.round(carrier * vegetablePercent / 100.0);
        flavors.replaceAll((key, amount) -> amount * 4000);
        return new LiquidBlend(vg * 4000, (carrier - vg) * 4000, nic * 4000, flavors);
    }
    private int[] doses(LiquidBlend blend) {
        if (!blend.usable()) return null;
        int[] amounts = new int[6];
        if (blend.vegetable() % 4000 != 0 || blend.clear() % 4000 != 0 || blend.nicotine() % 4000 != 0) return null;
        amounts[0] = blend.vegetable() / 4000; amounts[1] = blend.clear() / 4000; amounts[2] = blend.nicotine() / 4000;
        for (var entry : blend.flavors().entrySet()) {
            if (entry.getValue() % 4000 != 0) return null;
            int remaining = entry.getValue() / 4000;
            for (int i = 3; i < 6; i++) {
                String flavor = flavor(tanks[i].getFluid());
                if (entry.getKey().equals(flavor)) {
                    amounts[i] = Math.min(remaining, tanks[i].getFluidAmount()); remaining -= amounts[i];
                }
            }
            if (remaining != 0) return null;
        }
        for (int i = 0; i < 6; i++) if (tanks[i].getFluidAmount() < amounts[i]) return null;
        return amounts;
    }
    public boolean canBlend(LiquidBlend blend) {
        return !inventory.getStackInSlot(6).isEmpty() && doses(blend) != null
                && inventory.insertItem(OUTPUT, output(blend), true).isEmpty();
    }
    private ItemStack output(LiquidBlend blend) {
        ItemStack result = VaporData.bottle(blend, 250);
        var name = inventory.getStackInSlot(CARD).get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        if (name != null) result.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, name);
        return result;
    }
    private void blend(LiquidBlend blend) {
        int[] amounts = doses(blend);
        if (amounts == null || !canBlend(blend)) return;
        ItemStack result = output(blend);
        for (int i = 0; i < 6; i++) tanks[i].drain(amounts[i], IFluidHandler.FluidAction.EXECUTE);
        inventory.extractItem(6, 1, false); inventory.insertItem(OUTPUT, result, false); setChanged();
    }
    public void button(int id) {
        if (!isBlending()) return;
        if (id == 0 || id == 1) {
            vegetablePercent = Math.clamp(vegetablePercent + (id == 0 ? -10 : 10), 0, 100);
            inventory.getStackInSlot(CARD).remove(VaporContent.BLEND.get());
        } else if (id == 2) {
            strength = (strength + 1) % 4; inventory.getStackInSlot(CARD).remove(VaporContent.BLEND.get());
        } else if (id == 3) {
            automatic = !automatic;
        } else if (id == 4 && !inventory.getStackInSlot(CARD).isEmpty()) {
            inventory.getStackInSlot(CARD).set(VaporContent.BLEND.get(), desiredBlend());
        } else if (id == 5 && desiredBlend() != null) blend(desiredBlend());
        else if (id >= 6 && id < 9) {
            int slot = id - 3, amount = Math.min(250, tanks[slot].getFluidAmount());
            String flavor = flavor(tanks[slot].getFluid());
            if (amount > 0 && flavor != null && !inventory.getStackInSlot(6).isEmpty()) {
                ItemStack returned = VaporData.bottle(LiquidBlend.essence(flavor), amount);
                if (inventory.insertItem(OUTPUT, returned, true).isEmpty()) {
                    tanks[slot].drain(amount, IFluidHandler.FluidAction.EXECUTE);
                    inventory.extractItem(6, 1, false); inventory.insertItem(OUTPUT, returned, false);
                }
            }
        }
        progress = 0; activeBlend = null; setChanged();
    }
    private String flavor(FluidStack fluid) {
        LiquidBlend blend = VaporData.blend(fluid);
        return blend != null && blend.vegetable() == 0 && blend.clear() == 0 && blend.nicotine() == 0
                && blend.flavors().size() == 1 ? blend.flavors().keySet().iterator().next() : null;
    }
    public IFluidHandler fluidHandler() {
        return new IFluidHandler() {
            @Override public int getTanks() { return 6; }
            @Override public FluidStack getFluidInTank(int tank) { return tanks[tank].getFluid().copy(); }
            @Override public int getTankCapacity(int tank) { return 2000; }
            @Override public boolean isFluidValid(int tank, FluidStack stack) { return validFluid(tank, stack); }
            @Override public int fill(FluidStack stack, FluidAction action) {
                int filled = 0;
                for (FluidTank tank : tanks) {
                    int accepted = tank.fill(stack.copyWithAmount(stack.getAmount() - filled), action);
                    filled += accepted; if (filled == stack.getAmount()) break;
                }
                return filled;
            }
            @Override public FluidStack drain(int max, FluidAction action) { return FluidStack.EMPTY; }
            @Override public FluidStack drain(FluidStack stack, FluidAction action) { return FluidStack.EMPTY; }
        };
    }
    public CompoundTag portableTanks(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 6; i++) tag.put("Tank" + i, tanks[i].writeToNBT(lookup, new CompoundTag()));
        tag.putInt("Vegetable", vegetablePercent); tag.putInt("Strength", strength);
        return tag;
    }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) { return portableTanks(lookup); }
    public void dropContents() {
        for (int i = 0; i < SLOT_COUNT; i++) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inventory.getStackInSlot(i));
        // The block loot copies portableTanks onto the station item before removal.
    }
    @Override public Component getDisplayName() { return Component.translatable(isBlending() ? "block.tobacconistmod.liquid_blending_bench" : "block.tobacconistmod.extraction_station"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) { return new LiquidStationMenu(id, playerInventory, this); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup); tag.put("Inventory", inventory.serializeNBT(lookup));
        for (int i = 0; i < 6; i++) tag.put("Tank" + i, tanks[i].writeToNBT(lookup, new CompoundTag()));
        tag.putInt("Burn", burn); tag.putInt("Vegetable", vegetablePercent); tag.putInt("Strength", strength);
        tag.putBoolean("Automatic", automatic);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup); inventory.deserializeNBT(lookup, tag.getCompound("Inventory"));
        for (int i = 0; i < 6; i++) tanks[i].readFromNBT(lookup, tag.getCompound("Tank" + i));
        burn = Math.clamp(tag.getInt("Burn"), 0, 32767); vegetablePercent = Math.clamp(tag.getInt("Vegetable"), 0, 100);
        strength = Math.clamp(tag.getInt("Strength"), 0, 3); automatic = tag.getBoolean("Automatic");
        progress = 0; activeRecipe = null; activeBlend = null;
    }
}
