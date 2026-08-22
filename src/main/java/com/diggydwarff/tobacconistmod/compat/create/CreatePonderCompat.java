package com.diggydwarff.tobacconistmod.compat.create;

import com.diggydwarff.tobacconistmod.compat.create.ponder.TobacconistPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

/** Client-only bridge that registers Tobacconist's Create Ponder plugin. */
public final class CreatePonderCompat {
    private CreatePonderCompat() {}

    public static void register() {
        PonderIndex.addPlugin(new TobacconistPonderPlugin());
    }
}
