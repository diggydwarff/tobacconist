package com.diggydwarff.tobacconistmod.vapor;

public final class VaporCloudVerification {
    public static void main(String[] args) {
        for (int draw = 6; draw <= 40; draw++) {
            var previous = VaporCloudProfile.of(0, draw);
            for (int vg = 1; vg <= 100; vg++) {
                var current = VaporCloudProfile.of(vg, draw);
                require(current.particles() >= previous.particles() && current.size() >= previous.size()
                        && current.lifetime() >= previous.lifetime() && current.opacity() >= previous.opacity(), "VG must never reduce clouds");
                require(current.particles() <= 123 && current.lifetime() <= 130 && current.duration() <= 16, "emission bounds");
                previous = current;
            }
        }
        var low = VaporCloudProfile.of(20, 40); var high = VaporCloudProfile.of(90, 40);
        require(high.particles() > low.particles() * 3 && high.size() > low.size() * 2
                && high.lifetime() > low.lifetime() * 1.5, "high VG produces substantially fuller clouds");
        require(VaporCloudProfile.of(-50, 0).equals(VaporCloudProfile.of(0, 6))
                && VaporCloudProfile.of(500, 1000).equals(VaporCloudProfile.of(100, 40)), "malformed packet values are clamped");
        for (int vg=0; vg<=100; vg++) {
            var slim=VaporCloudProfile.of(vg,40,65);
            var lowPower=VaporCloudProfile.of(vg,40,75);
            var pen=VaporCloudProfile.of(vg,40,100);
            var highPower=VaporCloudProfile.of(vg,40,150);
            require(slim.particles()<=lowPower.particles() && lowPower.particles()<=pen.particles()
                    && pen.particles()<highPower.particles() && slim.size()<pen.size() && pen.size()<highPower.size(),"device output scales cloud count and size");
            require(highPower.particles()<=185 && highPower.lifetime()<=130 && highPower.duration()<=16,"high-power emission remains bounded");
            require(VaporCloudProfile.of(vg,40,Integer.MIN_VALUE).equals(slim)
                    && VaporCloudProfile.of(vg,40,Integer.MAX_VALUE).equals(highPower),"malformed power values clamped");
        }
        System.out.println("Vapor cloud verification passed: VG scaling, device output/wattage, full-draw contrast, and bounded inputs/emission.");
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
