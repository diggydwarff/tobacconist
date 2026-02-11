package com.diggydwarff.tobacconistmod.datagen.items.custom;

import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ECigarette extends SmokingItem {

    public ECigarette(Properties properties) {
        super(properties);
    }

    // Use the cigarette and puff some smoke!
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {

            ServerLevel serverLevel = (ServerLevel) level;
            this.triggerSmokingEffectPlayer(player, serverLevel, 0);

            if(player.getItemInHand(hand).getDamageValue() >= getMaxDamage(player.getItemInHand(hand))){
                player.getItemInHand(hand).hurtAndBreak(1, player, (myPlayer -> myPlayer.broadcastBreakEvent(myPlayer.getUsedItemHand())));
            }
            player.getItemInHand(hand).setDamageValue(player.getItemInHand(hand).getDamageValue() + 1);

        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
        CompoundTag compoundtag = stack.getTag();

        if(compoundtag != null){

            String tobacco = compoundtag.getString("tobacco");

            if(!tobacco.isEmpty()){
                tooltip.add(Component.literal(tobacco.replace("[","").replace("]","")).withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.literal("Creative Tobacco").withStyle(ChatFormatting.GOLD));
            }

            super.appendHoverText(stack, worldIn, tooltip, flagIn);
        }
    };
}
