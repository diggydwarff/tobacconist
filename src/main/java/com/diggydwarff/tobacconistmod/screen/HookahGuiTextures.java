package com.diggydwarff.tobacconistmod.screen;

import com.diggydwarff.tobacconistmod.TobacconistMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Hookah GUI sprite segments.
 *
 * <p>Structured like Create's AllGuiTextures: a texture location plus U/V and
 * width/height for each independently rendered GUI segment. This class uses
 * only Minecraft/Tobacconist classes, so Create remains optional at runtime.</p>
 */
public enum HookahGuiTextures {
    HOOKAH("hookah", 0, 0, 200, 105),
    FUEL("hookah", 200, 0, 8, 63),
    PLAYER_INVENTORY("player_inventory", 0, 0, 176, 108);

    public static final int TITLE_COLOR = 0x592424;
    public static final int INVENTORY_TITLE_COLOR = 0x404040;

    private final ResourceLocation location;
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;

    HookahGuiTextures(String texture, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(
                TobacconistMod.MODID,
                "textures/gui/" + texture + ".png"
        );
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    public ResourceLocation location() { return location; }
    public int startX() { return startX; }
    public int startY() { return startY; }
    public int width() { return width; }
    public int height() { return height; }
}
