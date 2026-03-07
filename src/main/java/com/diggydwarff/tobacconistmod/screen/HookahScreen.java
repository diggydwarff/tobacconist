package com.diggydwarff.tobacconistmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HookahScreen extends AbstractContainerScreen<HookahMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TobacconistMod.MODID, "textures/gui/hookah_gui.png");

    public HookahScreen(HookahMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        renderFuelLevel(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 105, y + 33, 176, 0, 8, menu.getScaledProgress());
        }
    }

    private void renderFuelLevel(GuiGraphics guiGraphics, int x, int y) {
        int h = menu.getScaledFuelLevel();
        if (h > 0) {
            guiGraphics.blit(
                    TEXTURE,
                    x + 156,
                    y + 14 + (63 - h),
                    176,
                    13 + (63 - h),
                    8,
                    h
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}