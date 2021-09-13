package dev.gigaherz.partycool.values;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class UniformRandomNumber extends VaryingNumber
{
    public static final Codec<UniformRandomNumber> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VaryingNumberRegistry.getCodec().fieldOf("minValue").forGetter(v -> v.minValue),
            VaryingNumberRegistry.getCodec().fieldOf("maxValue").forGetter(v -> v.maxValue)
    ).apply(instance, UniformRandomNumber::new));

    public VaryingNumber minValue;
    public VaryingNumber maxValue;

    public UniformRandomNumber()
    {

    }

    public UniformRandomNumber(VaryingNumber min, VaryingNumber max)
    {
        minValue = min;
        maxValue = max;
    }

    @Override
    public Codec<? extends VaryingNumber> getCodec()
    {
        return CODEC;
    }

    @Override
    public float getValue(float time)
    {
        return (float) ThreadLocalRandom.current().nextDouble(minValue.getValue(time), maxValue.getValue(time));
    }
}
