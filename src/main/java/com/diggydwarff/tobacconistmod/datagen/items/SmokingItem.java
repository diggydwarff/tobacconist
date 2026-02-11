package com.diggydwarff.tobacconistmod.datagen.items;

import com.diggydwarff.tobacconistmod.effect.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public abstract class SmokingItem extends Item {

    public SmokingItem(Properties properties) {
        super(properties);
    }

    private String tooltip;

    public void setTooltip(String tooltip){
        this.tooltip = tooltip;
    }

    // Trigger smoking effect for a player
    public void triggerSmokingEffectPlayer(Player player, ServerLevel level, int smokelevel){

        Vec3 look = player.getLookAngle();
        Vec3 eyePos = new Vec3(player.getX(), player.getY()+1.4, player.getZ());

        look.multiply(0.3D, 0.3D, 0.3D);
        eyePos.add(look);
        look.multiply(0.066D, 0.066D, 0.066D);

        level.playSound(
                null,
                player.getX(), player.getY() + 1.4, player.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.05F, //volume
                1.0F
        );

        Random rand = new Random();
        for (int i = 0; i < 5; ++i)
        {
            Vec3 newVec = (new Vec3(rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D));
            newVec.multiply(0.01D, 0.01D, 0.01D);
            Vec3 mergeVec = look.add(newVec);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX()+mergeVec.x, player.getY()+1.4+mergeVec.y, player.getZ()+mergeVec.z, smokelevel, 0, 0, 0, 0);
        }

        player.addEffect(new MobEffectInstance(ModEffects.NICOTINE.get(), 500, 0));

    }

}
