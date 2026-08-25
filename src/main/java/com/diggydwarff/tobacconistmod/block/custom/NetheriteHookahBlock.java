package com.diggydwarff.tobacconistmod.block.custom;

import com.mojang.serialization.MapCodec;

/** Netherite Hookah variant; ambient particles are emitted by HookahEntity client ticks. */
public class NetheriteHookahBlock extends DoubleHookahBlock {
    @Override
    protected MapCodec<? extends DoubleHookahBlock> codec() {
        return simpleCodec(NetheriteHookahBlock::new);
    }

    public NetheriteHookahBlock(Properties properties) {
        super(properties);
    }
}
