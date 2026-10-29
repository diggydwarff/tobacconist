package com.diggydwarff.tobacconistmod.backport;

import com.mojang.serialization.Codec;

/** Typed codec for Forge NBT. No 1.21 data-component registry is required. */
public final class DataComponentType<T> {
    String name;
    final Codec<T> codec;
    private DataComponentType(Codec<T> codec) { this.codec = codec; }
    public static <T> Builder<T> builder() { return new Builder<>(); }
    public static final class Builder<T> {
        private Codec<T> codec;
        public Builder<T> persistent(Codec<T> codec) { this.codec = codec; return this; }
        public Builder<T> networkSynchronized(Object unused) { return this; }
        public DataComponentType<T> build() { return new DataComponentType<>(codec); }
    }
}
