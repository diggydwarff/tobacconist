package com.diggydwarff.tobacconistmod.novelty;

import java.util.ArrayList;
import java.util.List;

/** A short whistle for taps and one of two five-note phrases for a completed draw. */
public final class WhistleMelody {
    public record Note(int delay, float pitch) {}
    private static final int[][] PHRASES = {{0,4,7,12,7},{0,7,4,9,7}};
    private static final int[] DELAYS = {0,4,8,12,18};

    private WhistleMelody() {}

    public static List<Note> notes(int draw, int variant) {
        if (draw < 30) return List.of(new Note(0,.8F+Math.clamp(draw,1,30)/60F));
        int[] phrase=PHRASES[Math.floorMod(variant,PHRASES.length)];
        List<Note> notes=new ArrayList<>(phrase.length);
        for (int i=0;i<phrase.length;i++) {
            notes.add(new Note(DELAYS[i],.75F*(float)Math.pow(2,phrase[i]/12.0)));
        }
        return List.copyOf(notes);
    }
}
