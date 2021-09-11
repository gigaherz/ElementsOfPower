package gigaherz.partycool;

import com.mojang.serialization.Codec;

public interface ICodecSerializable<T>
{
    Codec<? extends T> getCodec();
}
