package com.diggydwarff.tobacconistmod.backport;

/** Java 17 equivalents of the bounded arithmetic used by the shared 4.2 logic. */
public final class BackportMath {
    private BackportMath() {}
    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static int clamp(long value, int min, int max) { return (int)Math.max(min, Math.min(max, value)); }
    public static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    public static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
}
