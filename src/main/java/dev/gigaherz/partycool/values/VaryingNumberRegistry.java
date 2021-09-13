package dev.gigaherz.partycool.values;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class VaryingNumberRegistry
{
    private static BiMap<String, Codec<? extends VaryingNumber>> REGISTRY = HashBiMap.create();

    public static Codec<VaryingNumber> getCodec()
    {
        return new Codec<VaryingNumber>()
        {
            @Override
            public <T> DataResult<Pair<VaryingNumber, T>> decode(DynamicOps<T> ops, T input)
            {
                return null;
            }

            @Override
            public <T> DataResult<T> encode(VaryingNumber input, DynamicOps<T> ops, T prefix)
            {
                return null;
            }
        };
    }
}
