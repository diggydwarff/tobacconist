package com.diggydwarff.tobacconistmod.vapor;

/** Bounded visual parameters shared by emission and particle growth. */
public record VaporCloudProfile(int particles, int duration, int lifetime, float size, float opacity) {
    public static VaporCloudProfile of(int vegetable, int draw) {
        return of(vegetable, draw, 100);
    }

    public static VaporCloudProfile of(int vegetable, int draw, int output) {
        float vg = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(vegetable, 0, 100) / 100F;
        float strength = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(draw, 6, 40) / 40F;
        float density = vg * vg;
        float power = com.diggydwarff.tobacconistmod.backport.BackportMath.clamp(output, 40, 150) / 100F;
        return new VaporCloudProfile(Math.round((18 + 105 * density) * (.35F + .65F * strength) * power),
                Math.round(6 + 10 * strength), Math.round(32 + 78 * vg + 20 * strength),
                (.14F + .48F * density) * (.6F + .4F * strength) * (float) Math.sqrt(power), .40F + .40F * vg);
    }
}
