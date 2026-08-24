package com.diggydwarff.tobacconistmod.block.custom;

import com.mojang.serialization.MapCodec;

/** Prestige Hookah. Ambient particles are driven by HookahEntity client ticks. */
public class NetheriteHookahBlock extends DoubleHookahBlock {
    @Override
    protected MapCodec<? extends DoubleHookahBlock> codec() {
        return simpleCodec(NetheriteHookahBlock::new);
    }

    public NetheriteHookahBlock(Properties properties) {
        super(properties);
    }
}
