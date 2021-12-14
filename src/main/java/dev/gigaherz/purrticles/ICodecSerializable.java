package dev.gigaherz.purrticles;

import com.mojang.serialization.Codec;

public interface ICodecSerializable<T>
{
    Codec<? extends T> getCodec();
}
