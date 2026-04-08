package com.diggydwarff.tobacconistmod.client;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import com.diggydwarff.tobacconistmod.config.TobacconistConfig;
import com.diggydwarff.tobacconistmod.datagen.items.SmokingItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = TobacconistMod.MODID, value = Dist.CLIENT)
public class ClientSmokingParticles {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ModList.get().isLoaded("curios")) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        if ((mc.level.getGameTime() % 10) != 0) return;

        for (Player player : mc.level.players()) {
            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                var handler = inv.getCurios().get("mouth");
                if (handler == null) return;

                ItemStack stack = handler.getStacks().getStackInSlot(0);
                if (stack.isEmpty()) return;
                if (!(stack.getItem() instanceof SmokingItem smokingItem)) return;
                if (!smokingItem.shouldEmitMouthSmoke(stack)) return;

                spawnMouthSmoke(mc, player);
            });
        }
    }

    private static void spawnMouthSmoke(Minecraft mc, Player player) {
        float yaw = player.getYHeadRot() * Mth.DEG_TO_RAD;

        double side = 0.11D;
        double forward = 0.20D;
        double height = 1.52D;

        double x = player.getX() - Mth.sin(yaw) * forward + Mth.cos(yaw) * side;
        double y = player.getY() + height;
        double z = player.getZ() + Mth.cos(yaw) * forward + Mth.sin(yaw) * side;

        double vx = -Mth.sin(yaw) * 0.008D + (mc.level.random.nextDouble() - 0.5D) * 0.003D;
        double vy = 0.012D + mc.level.random.nextDouble() * 0.004D;
        double vz =  Mth.cos(yaw) * 0.008D + (mc.level.random.nextDouble() - 0.5D) * 0.003D;

        int density = Math.max(1, TobacconistConfig.CLIENT.particleDensity.get());
        if (mc.level.random.nextInt(density) == 0) {
            mc.level.addParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y + 0.01D, z,
                    vx * 0.6D, vy * 0.8D, vz * 0.6D
            );
        }
    }
}