package dev.gigaherz.elementsofpower.gemstones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.Objects;

public enum BiomeValue implements StringRepresentable
{
    NEUTRAL("neutral", 1),
    FOR("for", 2),
    AGAINST("against", 0);

    public static BiomeValue from(boolean positive, boolean negative)
    {
        if (positive && !negative)
            return FOR;
        if (negative && !positive)
            return AGAINST;
        return NEUTRAL;
    }

    private final String name;
    private final int value;

    BiomeValue(String name, int value)
    {
        this.name = name;
        this.value = value;
    }

    public int value()
    {
        return value;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    @Nullable
    public static BiomeValue byName(String name)
    {
        for(var entry : values())
        {
            if (entry.name.equals(name))
                return entry;
        }
        return null;
    }
    public static final Codec<BiomeValue> CODEC = Codec.STRING.comapFlatMap(
            str -> {
                var value = BiomeValue.byName(str);
                return value != null ? DataResult.success(value) : DataResult.error(() -> "No BiomeValue found for name '" + str + "'");
            },
            BiomeValue::name
    );
}
