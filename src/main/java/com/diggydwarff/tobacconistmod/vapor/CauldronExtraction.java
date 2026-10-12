package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Recipe-driven extraction in a vanilla water cauldron. No new block, block entity, or menu. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class CauldronExtraction extends SavedData {
    private static final Factory<CauldronExtraction> FACTORY = new Factory<>(CauldronExtraction::new, CauldronExtraction::load);
    private final Map<BlockPos, Batch> batches = new HashMap<>();

    public static CauldronExtraction get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, TobacconistMod.MODID + "_cauldron_extraction");
    }

    static final class Batch {
        final ResourceLocation recipe;
        final int[] amounts = new int[3];
        final List<ItemStack> receipts = new ArrayList<>();
        int progress;
        Batch(ResourceLocation recipe) { this.recipe = recipe; }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        InteractionResult result = use(level, event.getPos(), event.getEntity(), event.getHand());
        if (result.consumesAction()) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(level, event.getPos()));
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    public static InteractionResult use(ServerLevel level, BlockPos pos, Player player, InteractionHand hand) {
        if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON)) return InteractionResult.PASS;
        CauldronExtraction data = get(level);
        Batch batch = data.batches.get(pos);
        ItemStack held = player.getItemInHand(hand);

        if (batch != null && held.is(Items.WATER_BUCKET)) return InteractionResult.PASS;
        if (batch != null && player.isShiftKeyDown() && held.isEmpty()) {
            for (ItemStack receipt : batch.receipts) give(player, receipt.copy());
            data.batches.remove(pos); data.setDirty();
            message(player, "cauldron_cancelled");
            return InteractionResult.SUCCESS;
        }
        if (batch == null) {
            var selected = level.getRecipeManager().getAllRecipesFor(VaporContent.EXTRACTION.get()).stream()
                    .sorted(Comparator.comparing(holder -> holder.id().toString()))
                    .filter(holder -> {
                        int index = next(holder.value(), new Batch(holder.id()));
                        return index >= 0 && holder.value().inputs().get(index).ingredient().test(held);
                    }).findFirst();
            if (selected.isEmpty()) return InteractionResult.PASS;
            batch = new Batch(selected.get().id());
            data.batches.put(pos.immutable(), batch);
        }

        ExtractionRecipe recipe = recipe(level, batch);
        if (recipe == null) {
            message(player, "cauldron_recipe_missing");
            return InteractionResult.SUCCESS;
        }
        int next = next(recipe, batch);
        if (next >= 0) {
            var input = recipe.inputs().get(next);
            if (!input.ingredient().test(held)) {
                var examples = input.ingredient().getItems();
                if (examples.length > 0) player.displayClientMessage(Component.translatable(
                        "tobacconistmod.vapor.cauldron_next", input.count() - batch.amounts[next], examples[0].getHoverName()), true);
                return InteractionResult.SUCCESS;
            }
            int amount = Math.min(input.count() - batch.amounts[next], held.getCount());
            ItemStack receipt = held.copyWithCount(amount);
            batch.receipts.add(receipt); batch.amounts[next] += amount;
            if (!player.getAbilities().instabuild) held.shrink(amount);
            data.setDirty();
            message(player, next(recipe, batch) < 0 ? heated(level, pos) ? "cauldron_heating" : "cauldron_heat_needed" : "cauldron_added");
            return InteractionResult.SUCCESS;
        }
        if (batch.progress < recipe.duration()) {
            message(player, heated(level, pos) ? "cauldron_heating" : "cauldron_heat_needed");
            return InteractionResult.SUCCESS;
        }
        if (!held.is(Items.GLASS_BOTTLE)) {
            player.displayClientMessage(Component.translatable("tobacconistmod.vapor.cauldron_ready", recipe.result().getHoverName()), true);
            return InteractionResult.SUCCESS;
        }

        int water = waterCost(recipe);
        int available = level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL);
        if (available < water) { message(player, "cauldron_water_needed"); return InteractionResult.SUCCESS; }
        if (!player.getAbilities().instabuild) held.shrink(1);
        give(player, recipe.result().copy());
        for (ItemStack receipt : batch.receipts) returnContainer(player, receipt);
        data.batches.remove(pos); data.setDirty();
        if (available == water) level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        else level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(LayeredCauldronBlock.LEVEL, available - water));
        message(player, "cauldron_collected");
        return InteractionResult.SUCCESS;
    }

    static ExtractionRecipe recipe(ServerLevel level, Batch batch) {
        return level.getRecipeManager().byKey(batch.recipe)
                .filter(holder -> holder.value() instanceof ExtractionRecipe)
                .map(holder -> (ExtractionRecipe) holder.value()).orElse(null);
    }

    private static boolean water(ExtractionRecipe.Input input) {
        return input.ingredient().test(PotionContents.createItemStack(Items.POTION, Potions.WATER));
    }
    private static int waterCost(ExtractionRecipe recipe) {
        return Math.max(1, recipe.inputs().stream().filter(CauldronExtraction::water).mapToInt(ExtractionRecipe.Input::count).sum());
    }
    private static int next(ExtractionRecipe recipe, Batch batch) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            if (!water(recipe.inputs().get(i)) && batch.amounts[i] < recipe.inputs().get(i).count()) return i;
        }
        return -1;
    }
    public static boolean heated(ServerLevel level, BlockPos pos) {
        var below = level.getBlockState(pos.below());
        return below.getBlock() instanceof CampfireBlock && below.getValue(CampfireBlock.LIT)
                || below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.LAVA);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 5 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) get(level).advance(level, 5);
    }

    public void advance(ServerLevel level, int ticks) {
        for (var iterator = batches.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next(); BlockPos pos = entry.getKey(); Batch batch = entry.getValue();
            if (!level.hasChunkAt(pos)) continue;
            if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON)) {
                drop(level, pos, batch); iterator.remove(); setDirty(); continue;
            }
            ExtractionRecipe recipe = recipe(level, batch);
            if (recipe == null || next(recipe, batch) >= 0 || !heated(level, pos)
                    || batch.progress >= recipe.duration() || level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL) < waterCost(recipe)) continue;
            batch.progress = Math.min(recipe.duration(), batch.progress + ticks); setDirty();
            if (level.getGameTime() % 40 == 0) level.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + .5, pos.getY() + .95, pos.getZ() + .5, 1, .12, .01, .12, .005);
        }
    }

    @SubscribeEvent
    public static void broken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.isCanceled()) return;
        CauldronExtraction data = get(level);
        Batch batch = data.batches.remove(event.getPos());
        if (batch != null) { drop(level, event.getPos(), batch); data.setDirty(); }
    }
    private static void drop(ServerLevel level, BlockPos pos, Batch batch) {
        for (ItemStack receipt : batch.receipts) Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .8, pos.getZ() + .5, receipt.copy());
    }
    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) player.drop(stack, false);
    }
    private static void returnContainer(Player player, ItemStack receipt) {
        ItemStack single = receipt.copyWithCount(1);
        var handler = single.getCapability(Capabilities.FluidHandler.ITEM);
        ItemStack remainder;
        if (handler != null && !handler.getFluidInTank(0).isEmpty()) {
            handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            remainder = handler.getContainer();
            if (remainder.is(single.getItem())) remainder = ItemStack.EMPTY;
        } else remainder = single.getCraftingRemainingItem();
        if (!remainder.isEmpty()) for (int i = 0; i < receipt.getCount(); i++) give(player, remainder.copy());
    }
    private static void message(Player player, String key) { player.displayClientMessage(Component.translatable("tobacconistmod.vapor." + key), true); }

    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag records = new ListTag();
        for (var entry : batches.entrySet()) {
            Batch batch = entry.getValue(); CompoundTag record = new CompoundTag();
            record.putLong("Pos", entry.getKey().asLong()); record.putString("Recipe", batch.recipe.toString());
            record.putIntArray("Amounts", batch.amounts); record.putInt("Progress", batch.progress);
            ListTag inputs = new ListTag(); batch.receipts.forEach(stack -> inputs.add(stack.save(lookup)));
            record.put("Inputs", inputs); records.add(record);
        }
        tag.put("Batches", records); return tag;
    }
    static CauldronExtraction load(CompoundTag tag, HolderLookup.Provider lookup) {
        CauldronExtraction data = new CauldronExtraction();
        for (Tag element : tag.getList("Batches", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) element;
            ResourceLocation id = ResourceLocation.tryParse(record.getString("Recipe"));
            if (id == null) continue;
            Batch batch = new Batch(id); int[] amounts = record.getIntArray("Amounts");
            for (int i = 0; i < Math.min(3, amounts.length); i++) batch.amounts[i] = Math.clamp(amounts[i], 0, 64);
            batch.progress = Math.clamp(record.getInt("Progress"), 0, 12000);
            for (Tag input : record.getList("Inputs", Tag.TAG_COMPOUND)) ItemStack.parse(lookup, input).ifPresent(batch.receipts::add);
            data.batches.put(BlockPos.of(record.getLong("Pos")), batch);
        }
        return data;
    }
}
