package dev.gigaherz.purrticles.values;

import dev.gigaherz.purrticles.ICodecSerializable;

public abstract class VaryingNumber implements ICodecSerializable<VaryingNumber>
{
    public abstract float getValue(float time);
}
