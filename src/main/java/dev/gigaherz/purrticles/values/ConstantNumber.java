package dev.gigaherz.purrticles.values;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ConstantNumber extends VaryingNumber
{
    public static final Codec<ConstantNumber> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(v -> v.value)
    ).apply(instance, ConstantNumber::new));

    public float value;

    public ConstantNumber()
    {

    }

    public ConstantNumber(float value)
    {
        this.value = value;
    }

    @Override
    public Codec<? extends VaryingNumber> getCodec()
    {
        return CODEC;
    }

    @Override
    public float getValue(float time)
    {
        return value;
    }
}
