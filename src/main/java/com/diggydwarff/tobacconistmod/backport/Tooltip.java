package com.diggydwarff.tobacconistmod.backport;

import net.minecraft.network.chat.Component;

/** Tooltip content for legacy widgets. */
public record Tooltip(Component text) {
    public static Tooltip create(Component text) { return new Tooltip(text); }
}
