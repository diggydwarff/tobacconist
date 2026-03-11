package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import com.diggydwarff.tobacconistmod.recipes.WoodenPipeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;

public class WoodenSmokingPipeItem extends SmokingItem {

    public  static final String NBT_TOBACCO = "PackedTobacco";
    public static final String NBT_PUFFS = "PuffsLeft";
    public static final int MAX_PUFFS = 40;

    public WoodenSmokingPipeItem(Properties properties) {
        super(properties);
    }

    private boolean isTobacco(ItemStack stack) {
        // Best: use a tag like #tobacconistmod:pipe_tobacco
        // return stack.is(ModTags.Items.PIPE_TOBACCO);

        return stack.getItem() == ModItems.TOBACCO_LOOSE_BURLEY.get()
                || stack.getItem() == ModItems.TOBACCO_LOOSE_ORIENTAL.get()
                || stack.getItem() == ModItems.TOBACCO_LOOSE_DOKHA.get()
                || stack.getItem() == ModItems.TOBACCO_LOOSE_SHADE.get()
                || stack.getItem() == ModItems.TOBACCO_LOOSE_VIRGINIA.get()
                || stack.getItem() == ModItems.TOBACCO_LOOSE_WILD.get();

    }

    @Override
    public Component getName(ItemStack stack) {
        var tag = stack.getTag();

        if (tag != null && tag.contains(WoodenPipeRecipe.NBT_WOOD_PLANK)) {
            String idString = tag.getString(WoodenPipeRecipe.NBT_WOOD_PLANK);
            ResourceLocation id = new ResourceLocation(idString);

            var item = BuiltInRegistries.ITEM.get(id);
            ItemStack plankStack = new ItemStack(item);

            // Example: "Oak Planks" → "Oak"
            String plankName = plankStack.getHoverName().getString();
            plankName = plankName.replace(" Planks", "");

            return Component.literal(plankName + " Smoking Pipe");
        }

        return super.getName(stack);
    }

    @Override
    public boolean shouldEmitMouthSmoke(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("PuffsLeft")) {
            return false;
        }

        int puffs = tag.getInt("PuffsLeft");
        return puffs > 0 && puffs < 40;
    }

    private boolean isPacked(ItemStack pipe) {
        var tag = pipe.getTag();
        return tag != null && tag.contains(NBT_TOBACCO) && tag.getInt(NBT_PUFFS) > 0;
    }

    private void pack(ItemStack pipe, ItemStack tobacco) {
        pipe.getOrCreateTag().putString(NBT_TOBACCO,
                BuiltInRegistries.ITEM.getKey(tobacco.getItem()).toString());
        pipe.getOrCreateTag().putInt(NBT_PUFFS, MAX_PUFFS);
    }

    private void unpack(ItemStack pipe) {
        var tag = pipe.getOrCreateTag();
        tag.remove(NBT_TOBACCO);
        tag.remove(NBT_PUFFS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pipe = player.getItemInHand(hand);

        // If pipe is in offhand AND player holding tobacco in main hand,
        // allow tobacco to handle packing instead of smoking
        if (hand == InteractionHand.OFF_HAND) {
            ItemStack main = player.getMainHandItem();
            if (!main.isEmpty() && main.getItem() instanceof LooseTobaccoItem) {
                return InteractionResultHolder.pass(pipe);
            }
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.consume(pipe); // prevents swing
        }

        CompoundTag tag = pipe.getTag();
        int puffs = (tag == null) ? 0 : tag.getInt("PuffsLeft");

        if (puffs <= 0) {
            return InteractionResultHolder.pass(pipe);
        }

        this.triggerSmokingEffectPlayer(player, (ServerLevel) level, 0, pipe);

        puffs--;

        if (puffs <= 0) {
            pipe.getOrCreateTag().remove("PuffsLeft");
            pipe.getOrCreateTag().remove("PackedTobacco");
        } else {
            pipe.getOrCreateTag().putInt("PuffsLeft", puffs);
        }

        return InteractionResultHolder.consume(pipe);
    }

    // Show "puffs left" on durability bar
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isPacked(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!isPacked(stack)) return 0;
        int puffs = stack.getOrCreateTag().getInt(NBT_PUFFS);
        return Math.round(13.0F * (puffs / (float) MAX_PUFFS));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55; // green
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();

        if (tag != null && tag.contains("PuffsLeft")) {
            tooltip.add(Component.literal("Puffs left: " + tag.getInt("PuffsLeft")));
            if (tag.contains("PackedTobacco")) {
                String id = tag.getString("PackedTobacco");

                Item packedItem = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
                ItemStack packedStack = new ItemStack(packedItem);

                tooltip.add(Component.literal("Packed: ")
                        .append(packedStack.getHoverName()));
            }
        } else {
            tooltip.add(Component.literal("Empty"));
            tooltip.add(Component.literal("Hold in offhand and right-click with tobacco in hand to pack"));
        }
    }

}
