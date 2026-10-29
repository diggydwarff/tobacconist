package com.diggydwarff.tobacconistmod.vapor;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
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
import com.diggydwarff.tobacconistmod.backport.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Recipe-driven extraction in a vanilla water cauldron. No new block, block entity, or menu. */
@EventBusSubscriber(modid = TobacconistMod.MODID)
public final class CauldronExtraction extends SavedData {
    private final Map<BlockPos, Batch> batches = new HashMap<>();

    public static CauldronExtraction get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CauldronExtraction::load, CauldronExtraction::new, TobacconistMod.MODID + "_cauldron_extraction");
    }

    static final class Batch {
        final ResourceLocation recipe;
        final int[] amounts = new int[3];
        final List<ItemStack> receipts = new ArrayList<>();
        final List<Integer> fluidUsed = new ArrayList<>();
        ItemStack result = ItemStack.EMPTY;
        int remainingLiquid;
        boolean legacyAmounts;
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
        if (level.getBlockEntity(pos) instanceof ExtractCauldronEntity cauldron) return cauldron.use(player, hand);
        if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON) && !level.getBlockState(pos).is(Blocks.CAULDRON)) return InteractionResult.PASS;
        CauldronExtraction data = get(level);
        Batch batch = data.batches.get(pos);
        if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON) && (batch == null || batch.result.isEmpty())) {
            FluidStack fluid = heldFluid(player.getItemInHand(hand));
            if (!fluid.isEmpty() && fluid.getFluid() == VaporContent.CRUDE_FLUID.get()) {
                message(player, "cauldron_brew_crude");
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);

        if (batch != null && held.is(Items.WATER_BUCKET)) return InteractionResult.PASS;
        if (batch != null && player.isShiftKeyDown() && held.isEmpty()) {
            for (ItemStack receipt : batch.receipts) give(player, receipt.copy());
            data.batches.remove(pos); data.setDirty();
            message(player, batch.result.isEmpty() ? "cauldron_cancelled" : "cauldron_spilled");
            return InteractionResult.SUCCESS;
        }
        if (batch != null && batch.result.isEmpty() && !validFluidReceipts(batch)) {
            for (ItemStack receipt : batch.receipts) give(player, receipt.copy());
            data.batches.remove(pos); data.setDirty();
            message(player, "cauldron_batch_refunded");
            return InteractionResult.SUCCESS;
        }
        if (batch != null && !batch.result.isEmpty()) {
            if (!heldFluid(batch.result).isEmpty()) {
                convert(level, pos, batch.result, batch.remainingLiquid, waterLevels(level, pos), List.of());
                data.batches.remove(pos); data.setDirty();
                return ((ExtractCauldronEntity) level.getBlockEntity(pos)).use(player, hand);
            }
            return collect(data, level, pos, player, hand, batch);
        }
        if (batch == null) {
            var selected = level.getRecipeManager().getAllRecipesFor(VaporContent.EXTRACTION.get()).stream().map(com.diggydwarff.tobacconistmod.backport.RecipeHolder::new)
                    .sorted(Comparator.comparing(holder -> holder.id().toString()))
                    .filter(holder -> {
                        int index = next(holder.value(), new Batch(holder.id()));
                        return index >= 0 && matches(holder.value().inputs().get(index), held);
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
            if (!matches(input, held)) {
                var examples = input.ingredient().getItems();
                if (examples.length > 0) player.displayClientMessage(Component.translatable(
                        "tobacconistmod.vapor.cauldron_next", Math.max(1, (int) Math.ceil((required(input) - batch.amounts[next]) / (double) unit(input))), examples[0].getHoverName()), true);
                return InteractionResult.SUCCESS;
            }
            FluidStack reference = inputFluid(input);
            int amount = Math.min(required(input) - batch.amounts[next], reference.isEmpty() ? held.getCount() : heldFluid(held).getAmount());
            if (!reference.isEmpty() && !canSpendFluid(held, amount)) {
                if (batch.receipts.isEmpty()) { data.batches.remove(pos); data.setDirty(); }
                player.displayClientMessage(Component.translatable("tobacconistmod.vapor.cauldron_partial_bottle", amount), true);
                return InteractionResult.SUCCESS;
            }
            ItemStack receipt = held.copyWithCount(reference.isEmpty() ? amount : 1);
            batch.receipts.add(receipt); batch.fluidUsed.add(reference.isEmpty() ? 0 : amount); batch.amounts[next] += amount;
            if (!player.getAbilities().instabuild) held.shrink(receipt.getCount());
            data.setDirty();
            message(player, next(recipe, batch) < 0 ? heated(level, pos) ? "cauldron_heating" : "cauldron_heat_needed" : "cauldron_added");
            return InteractionResult.SUCCESS;
        }
        if (batch.progress < recipe.duration()) {
            message(player, heated(level, pos) ? "cauldron_heating" : "cauldron_heat_needed");
            return InteractionResult.SUCCESS;
        }
        if (!heldFluid(recipe.result()).isEmpty() && waterLevels(level, pos) >= waterCost(recipe)) {
            convert(level, pos, recipe.result(), heldFluid(recipe.result()).getAmount(),
                    waterLevels(level, pos) - waterCost(recipe), returns(batch));
            data.batches.remove(pos); data.setDirty();
            return ((ExtractCauldronEntity) level.getBlockEntity(pos)).use(player, hand);
        }
        if (collection(recipe.result(), held, heldFluid(recipe.result()).getAmount()).isEmpty()) {
            player.displayClientMessage(Component.translatable("tobacconistmod.vapor.cauldron_ready", recipe.result().getHoverName()), true);
            return InteractionResult.SUCCESS;
        }

        int water = waterCost(recipe);
        int available = level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL);
        if (available < water) { message(player, "cauldron_water_needed"); return InteractionResult.SUCCESS; }
        batch.result = recipe.result().copy(); batch.remainingLiquid = heldFluid(batch.result).getAmount();
        for (int i = 0; i < batch.receipts.size(); i++) returnContainer(player, batch.receipts.get(i), batch.fluidUsed.get(i));
        batch.receipts.clear(); batch.fluidUsed.clear(); data.setDirty();
        if (available == water) level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        else level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(LayeredCauldronBlock.LEVEL, available - water));
        return collect(data, level, pos, player, hand, batch);
    }

    record Collection(ItemStack bottle, int amount) {}

    static java.util.Optional<Collection> collection(ItemStack result, ItemStack target, int remaining) {
        FluidStack fluid = heldFluid(result);
        if (fluid.isEmpty()) return target.is(Items.GLASS_BOTTLE)
                ? java.util.Optional.of(new Collection(result.copy(), 0)) : java.util.Optional.empty();
        if (remaining <= 0) return java.util.Optional.empty();
        fluid = com.diggydwarff.tobacconistmod.backport.FluidStacks.withAmount(fluid, remaining);
        ItemStack filled;
        int amount;
        if (target.is(Items.GLASS_BOTTLE) && remaining < VaporData.BOTTLE && VaporBottleHandler.supported(fluid)) {
            amount = remaining; filled = VaporBottleHandler.filled(fluid);
        } else {
            var handler = target.copyWithCount(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if (handler == null) return java.util.Optional.empty();
            amount = handler.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
            if (amount <= 0) return java.util.Optional.empty();
            handler.fill(com.diggydwarff.tobacconistmod.backport.FluidStacks.withAmount(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
            filled = handler.getContainer();
        }
        var name = com.diggydwarff.tobacconistmod.backport.ComponentData.get(result, com.diggydwarff.tobacconistmod.backport.DataComponents.CUSTOM_NAME);
        if (name != null) com.diggydwarff.tobacconistmod.backport.ComponentData.set(filled, com.diggydwarff.tobacconistmod.backport.DataComponents.CUSTOM_NAME, name);
        return java.util.Optional.of(new Collection(filled, amount));
    }

    private static InteractionResult collect(CauldronExtraction data, ServerLevel level, BlockPos pos, Player player, InteractionHand hand, Batch batch) {
        ItemStack held = player.getItemInHand(hand);
        var transfer = collection(batch.result, held, batch.remainingLiquid);
        if (transfer.isEmpty()) {
            player.displayClientMessage(Component.translatable("tobacconistmod.vapor.cauldron_ready", batch.result.getHoverName()), true);
            return InteractionResult.SUCCESS;
        }
        if (held.getCount() == 1 && !player.getAbilities().instabuild) player.setItemInHand(hand, transfer.get().bottle());
        else {
            if (!player.getAbilities().instabuild) held.shrink(1);
            give(player, transfer.get().bottle());
        }
        batch.remainingLiquid -= transfer.get().amount();
        if (batch.remainingLiquid <= 0) data.batches.remove(pos);
        data.setDirty(); message(player, "cauldron_collected");
        return InteractionResult.SUCCESS;
    }

    static FluidStack heldFluid(ItemStack stack) {
        var handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        return handler == null ? FluidStack.EMPTY : handler.getFluidInTank(0);
    }
    private static FluidStack inputFluid(ExtractionRecipe.Input input) {
        for (ItemStack example : input.ingredient().getItems()) {
            FluidStack fluid = heldFluid(example);
            if (!fluid.isEmpty()) return fluid;
        }
        return FluidStack.EMPTY;
    }
    private static int unit(ExtractionRecipe.Input input) {
        FluidStack fluid = inputFluid(input);
        return fluid.isEmpty() ? 1 : fluid.getAmount();
    }
    private static int required(ExtractionRecipe.Input input) { return input.count() * unit(input); }
    private static boolean matches(ExtractionRecipe.Input input, ItemStack stack) {
        FluidStack reference = inputFluid(input);
        return reference.isEmpty() ? input.ingredient().test(stack)
                : com.diggydwarff.tobacconistmod.backport.FluidStacks.same(reference, heldFluid(stack));
    }

    private static boolean canSpendFluid(ItemStack stack, int amount) {
        var handler = stack.copyWithCount(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null || amount <= 0) return false;
        FluidStack current = handler.getFluidInTank(0);
        FluidStack drained = handler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
        return drained.getAmount() == amount && com.diggydwarff.tobacconistmod.backport.FluidStacks.same(current, drained);
    }

    /** Older fixed-batch bottles cannot represent a partial remainder. Refund affected old batches. */
    private static boolean validFluidReceipts(Batch batch) {
        for (int i = 0; i < batch.receipts.size(); i++)
            if (batch.fluidUsed.get(i) > 0 && !canSpendFluid(batch.receipts.get(i), batch.fluidUsed.get(i))) return false;
        return true;
    }

    static ExtractionRecipe recipe(ServerLevel level, Batch batch) {
        ExtractionRecipe recipe = level.getRecipeManager().byKey(batch.recipe).map(com.diggydwarff.tobacconistmod.backport.RecipeHolder::new)
                .filter(holder -> holder.value() instanceof ExtractionRecipe)
                .map(holder -> (ExtractionRecipe) holder.value()).orElse(null);
        if (recipe != null && batch.legacyAmounts) {
            for (int i = 0; i < recipe.inputs().size(); i++) batch.amounts[i] *= unit(recipe.inputs().get(i));
            batch.legacyAmounts = false; get(level).setDirty();
        }
        return recipe;
    }

    private static boolean water(ExtractionRecipe.Input input) {
        return input.ingredient().test(PotionContents.createItemStack(Items.POTION, Potions.WATER));
    }
    private static int waterCost(ExtractionRecipe recipe) {
        return Math.max(1, recipe.inputs().stream().filter(CauldronExtraction::water).mapToInt(ExtractionRecipe.Input::count).sum());
    }
    private static int next(ExtractionRecipe recipe, Batch batch) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            if (!water(recipe.inputs().get(i)) && batch.amounts[i] < required(recipe.inputs().get(i))) return i;
        }
        return -1;
    }
    public static boolean heated(ServerLevel level, BlockPos pos) {
        var below = level.getBlockState(pos.below());
        return below.getBlock() instanceof CampfireBlock && below.getValue(CampfireBlock.LIT)
                || below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.LAVA);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % 5 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) get(level).advance(level, 5);
    }

    public void advance(ServerLevel level, int ticks) {
        for (var iterator = batches.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next(); BlockPos pos = entry.getKey(); Batch batch = entry.getValue();
            if (!level.hasChunkAt(pos)) continue;
            if (!level.getBlockState(pos).is(Blocks.WATER_CAULDRON)
                    && !(level.getBlockState(pos).is(Blocks.CAULDRON) && !batch.result.isEmpty())) {
                drop(level, pos, batch); iterator.remove(); setDirty(); continue;
            }
            if (!batch.result.isEmpty()) {
                if (!heldFluid(batch.result).isEmpty()) {
                    convert(level, pos, batch.result, batch.remainingLiquid, waterLevels(level, pos), List.of());
                    iterator.remove(); setDirty();
                }
                continue;
            }
            if (!validFluidReceipts(batch)) {
                drop(level, pos, batch); iterator.remove(); setDirty(); continue;
            }
            ExtractionRecipe recipe = recipe(level, batch);
            if (recipe == null || next(recipe, batch) >= 0 || waterLevels(level, pos) < waterCost(recipe)) continue;
            if (batch.progress < recipe.duration()) {
                if (!heated(level, pos)) continue;
                batch.progress = Math.min(recipe.duration(), batch.progress + ticks); setDirty();
            }
            if (batch.progress >= recipe.duration() && !heldFluid(recipe.result()).isEmpty()) {
                convert(level, pos, recipe.result(), heldFluid(recipe.result()).getAmount(),
                        waterLevels(level, pos) - waterCost(recipe), returns(batch));
                iterator.remove(); setDirty(); continue;
            }
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
    static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) player.drop(stack, false);
    }
    private static int waterLevels(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.WATER_CAULDRON) ? state.getValue(LayeredCauldronBlock.LEVEL) : 0;
    }
    private static void convert(ServerLevel level, BlockPos pos, ItemStack output, int amount, int water, List<ItemStack> returns) {
        level.setBlockAndUpdate(pos, VaporContent.EXTRACT_CAULDRON.get().defaultBlockState());
        ((ExtractCauldronEntity) level.getBlockEntity(pos)).initialize(output, amount, water, returns);
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, net.minecraft.sounds.SoundSource.BLOCKS, .5F, 1F);
    }
    private static List<ItemStack> returns(Batch batch) {
        List<ItemStack> returns = new ArrayList<>();
        for (int i = 0; i < batch.receipts.size(); i++) returns.addAll(returnedContainers(batch.receipts.get(i), batch.fluidUsed.get(i)));
        return returns;
    }
    private static void returnContainer(Player player, ItemStack receipt, int fluidUsed) {
        for (ItemStack remainder : returnedContainers(receipt, fluidUsed)) give(player, remainder);
    }
    private static List<ItemStack> returnedContainers(ItemStack receipt, int fluidUsed) {
        ItemStack single = receipt.copyWithCount(1);
        var handler = single.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        ItemStack remainder;
        if (handler != null && !handler.getFluidInTank(0).isEmpty()) {
            handler.drain(fluidUsed > 0 ? fluidUsed : Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            remainder = handler.getContainer();
            if (fluidUsed == 0 && remainder.is(single.getItem())) remainder = ItemStack.EMPTY;
        } else remainder = single.getCraftingRemainingItem();
        List<ItemStack> returns = new ArrayList<>();
        if (!remainder.isEmpty()) for (int i = 0; i < receipt.getCount(); i++) returns.add(remainder.copy());
        return returns;
    }
    private static void message(Player player, String key) { player.displayClientMessage(Component.translatable("tobacconistmod.vapor." + key), true); }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag records = new ListTag();
        for (var entry : batches.entrySet()) {
            Batch batch = entry.getValue(); CompoundTag record = new CompoundTag();
            record.putLong("Pos", entry.getKey().asLong()); record.putString("Recipe", batch.recipe.toString());
            record.putIntArray("Amounts", batch.amounts); record.putInt("Progress", batch.progress);
            record.putInt("Format", batch.legacyAmounts ? 1 : 2);
            record.putIntArray("FluidUsed", batch.fluidUsed.stream().mapToInt(Integer::intValue).toArray());
            if (!batch.result.isEmpty()) { record.put("Result", batch.result.save(new CompoundTag())); record.putInt("RemainingLiquid", batch.remainingLiquid); }
            ListTag inputs = new ListTag(); batch.receipts.forEach(stack -> inputs.add(stack.save(new CompoundTag())));
            record.put("Inputs", inputs); records.add(record);
        }
        tag.put("Batches", records); return tag;
    }
    static CauldronExtraction load(CompoundTag tag) {
        CauldronExtraction data = new CauldronExtraction();
        for (Tag element : tag.getList("Batches", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) element;
            ResourceLocation id = ResourceLocation.tryParse(record.getString("Recipe"));
            if (id == null) continue;
            Batch batch = new Batch(id); int[] amounts = record.getIntArray("Amounts");
            batch.legacyAmounts = record.getInt("Format") < 2;
            for (int i = 0; i < Math.min(3, amounts.length); i++) batch.amounts[i] = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(amounts[i], 0, batch.legacyAmounts ? 64 : 64000);
            batch.progress = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(record.getInt("Progress"), 0, 12000);
            for (Tag input : record.getList("Inputs", Tag.TAG_COMPOUND)) java.util.Optional.of(ItemStack.of((CompoundTag)input)).ifPresent(batch.receipts::add);
            int[] used = record.getIntArray("FluidUsed");
            for (int i = 0; i < batch.receipts.size(); i++) batch.fluidUsed.add(i < used.length ? com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(used[i], 0, 64000) : 0);
            if (record.contains("Result")) batch.result = java.util.Optional.of(ItemStack.of(record.getCompound("Result"))).orElse(ItemStack.EMPTY);
            batch.remainingLiquid = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(record.getInt("RemainingLiquid"), 0, 64000);
            data.batches.put(BlockPos.of(record.getLong("Pos")), batch);
        }
        return data;
    }
}
