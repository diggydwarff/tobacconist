package com.diggydwarff.tobacconistmod.backport;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

public final class ByteBufCodecs {
    private ByteBufCodecs() {}
    public static final StreamCodec<ByteBuf,Boolean> BOOL=StreamCodec.of(ByteBuf::writeBoolean,ByteBuf::readBoolean);
    public static final StreamCodec<ByteBuf,Float> FLOAT=StreamCodec.of(ByteBuf::writeFloat,ByteBuf::readFloat);
    public static final StreamCodec<ByteBuf,Integer> VAR_INT=StreamCodec.of((b,v)->new FriendlyByteBuf(b).writeVarInt(v),b->new FriendlyByteBuf(b).readVarInt());
    public static <T> StreamCodec<FriendlyByteBuf,T> fromCodec(Codec<T> codec) {
        return StreamCodec.of((buf,value)->{
            CompoundTag wrapper=new CompoundTag();
            wrapper.put("value",codec.encodeStart(NbtOps.INSTANCE,value).getOrThrow(false,msg->{throw new IllegalArgumentException(msg);}));
            buf.writeNbt(wrapper);
        },buf->{CompoundTag tag=buf.readNbt();if(tag==null)throw new IllegalArgumentException("Missing packet data");
            return codec.parse(NbtOps.INSTANCE,tag.get("value")).getOrThrow(false,msg->{throw new IllegalArgumentException(msg);});});
    }
    public static <T> StreamCodec<FriendlyByteBuf,T> fromCodecWithRegistries(Codec<T> codec) { return fromCodec(codec); }
}
