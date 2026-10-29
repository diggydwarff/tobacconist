package com.diggydwarff.tobacconistmod.backport;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Typed encoders around Forge FriendlyByteBuf/Netty buffers. */
public interface StreamCodec<B,T> {
    void encode(B buffer, T value);
    T decode(B buffer);
    static <B,T> StreamCodec<B,T> of(BiConsumer<B,T> encoder, Function<B,T> decoder) {
        return new StreamCodec<>() {
            public void encode(B buffer,T value) { encoder.accept(buffer,value); }
            public T decode(B buffer) { return decoder.apply(buffer); }
        };
    }
    default <U> StreamCodec<B,U> map(Function<T,U> read,Function<U,T> write) {
        StreamCodec<B,T> self=this;
        return of((buffer,value)->self.encode(buffer,write.apply(value)),buffer->read.apply(self.decode(buffer)));
    }
    @SuppressWarnings("unchecked") default <C extends B> StreamCodec<C,T> cast() { return (StreamCodec<C,T>)this; }
    static <B,A,C,T> StreamCodec<B,T> composite(StreamCodec<B,A> a,Function<T,A> ga,
            StreamCodec<B,C> c,Function<T,C> gc,BiFunction<A,C,T> factory) {
        return of((buf,v)->{a.encode(buf,ga.apply(v));c.encode(buf,gc.apply(v));},buf->factory.apply(a.decode(buf),c.decode(buf)));
    }
}
