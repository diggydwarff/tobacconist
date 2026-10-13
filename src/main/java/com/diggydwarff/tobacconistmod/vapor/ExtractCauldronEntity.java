package com.diggydwarff.tobacconistmod.vapor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import java.util.ArrayList;
import java.util.List;

/** Completed extract and returned input containers live in the actual cauldron. */
public final class ExtractCauldronEntity extends BlockEntity {
    private ItemStack result = ItemStack.EMPTY;
    private int amount;
    private int originalAmount;
    private int waterLevels;
    private final List<ItemStack> returns = new ArrayList<>();
    private final IFluidHandler handler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return tank == 0 ? contents() : FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? originalAmount : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack fluid) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.isSameFluidSameComponents(resource, contents()) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int maximum, FluidAction action) {
            int drained = Math.min(Math.max(0, maximum), amount);
            FluidStack fluid = contents().copyWithAmount(drained);
            if (drained > 0 && action.execute()) take(drained, null);
            return fluid;
        }
    };

    public ExtractCauldronEntity(BlockPos pos, BlockState state) { super(VaporContent.EXTRACT_CAULDRON_ENTITY.get(), pos, state); }
    public IFluidHandler fluidHandler() { return handler; }
    public int amount() { return amount; }
    public FluidStack contents() { return CauldronExtraction.heldFluid(result).copyWithAmount(amount); }

    void initialize(ItemStack output, int remaining, int water, List<ItemStack> containers) {
        result = output.copy(); amount = remaining; originalAmount = Math.max(remaining, CauldronExtraction.heldFluid(output).getAmount());
        waterLevels = Math.clamp(water, 0, 3); returns.clear(); containers.forEach(stack -> returns.add(stack.copy()));
        changed();
    }
    public InteractionResult use(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            releaseReturns(player); amount = 0; restoreEmpty();
            player.displayClientMessage(Component.translatable("tobacconistmod.vapor.cauldron_spilled"), true);
            return InteractionResult.SUCCESS;
        }
        var transfer = CauldronExtraction.collection(result, player.getItemInHand(hand), amount);
        if (transfer.isEmpty()) {
            player.displayClientMessage(Component.translatable("tobacconistmod.vapor.cauldron_contents", contents().getHoverName(), amount), true);
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getCount() == 1 && !player.getAbilities().instabuild) player.setItemInHand(hand, transfer.get().bottle());
        else {
            if (!player.getAbilities().instabuild) held.shrink(1);
            CauldronExtraction.give(player, transfer.get().bottle());
        }
        take(transfer.get().amount(), player);
        player.awardStat(Stats.USE_CAULDRON);
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1F, 1F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, worldPosition);
        return InteractionResult.SUCCESS;
    }
    private void take(int drained, Player player) {
        if (level == null || level.isClientSide || drained <= 0 || drained > amount) return;
        amount -= drained; releaseReturns(player);
        if (amount == 0) restoreEmpty(); else changed();
    }
    private void releaseReturns(Player player) {
        List<ItemStack> pending = List.copyOf(returns); returns.clear(); setChanged();
        for (ItemStack stack : pending) {
            if (player != null) CauldronExtraction.give(player, stack.copy());
            else if (level != null && !level.isClientSide) Containers.dropItemStack(level,
                    worldPosition.getX() + .5, worldPosition.getY() + .8, worldPosition.getZ() + .5, stack.copy());
        }
    }
    void dropReturns() { if (level != null && !level.isClientSide) releaseReturns(null); }
    private void restoreEmpty() {
        setChanged();
        if (level == null || level.isClientSide) return;
        // The whole batch boils down: no original water remains after collection.
        BlockState state = Blocks.CAULDRON.defaultBlockState();
        level.setBlockAndUpdate(worldPosition, state);
    }
    private void changed() {
        setChanged();
        if (level == null || level.isClientSide || amount == 0) return;
        int fill = Math.clamp((amount + VaporData.BOTTLE - 1) / VaporData.BOTTLE, 1, 3);
        BlockState state = getBlockState().setValue(LayeredCauldronBlock.LEVEL, fill);
        level.setBlock(worldPosition, state, Block.UPDATE_ALL);
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        if (!result.isEmpty()) tag.put("Result", result.save(lookup));
        tag.putInt("Amount", amount); tag.putInt("OriginalAmount", originalAmount); tag.putInt("WaterLevels", waterLevels);
        ListTag pending = new ListTag(); returns.forEach(stack -> pending.add(stack.save(lookup))); tag.put("Returns", pending);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        result = tag.contains("Result") ? ItemStack.parse(lookup, tag.getCompound("Result")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        originalAmount = Math.clamp(tag.getInt("OriginalAmount"), 1, 64000);
        amount = Math.clamp(tag.getInt("Amount"), 0, originalAmount); waterLevels = Math.clamp(tag.getInt("WaterLevels"), 0, 3);
        returns.clear(); for (Tag input : tag.getList("Returns", Tag.TAG_COMPOUND)) ItemStack.parse(lookup, input).ifPresent(returns::add);
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) { return saveWithoutMetadata(lookup); }
    @Override public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider lookup) {
        loadWithComponents(packet.getTag(), lookup); refreshColor();
    }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookup) {
        loadWithComponents(tag, lookup); refreshColor();
    }
    private void refreshColor() {
        requestModelDataUpdate();
        if (level != null && level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
