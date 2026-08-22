package com.diggydwarff.tobacconistmod.util;

/** Immutable metadata snapshot for one variety participating in a blended tobacco. */
public record TobaccoBlendComponent(
        String variety,
        int quality,
        String cure,
        String flavorId
) {
}
