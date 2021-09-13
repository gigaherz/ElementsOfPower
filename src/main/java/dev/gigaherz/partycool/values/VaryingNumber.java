package dev.gigaherz.partycool.values;

import dev.gigaherz.partycool.ICodecSerializable;

public abstract class VaryingNumber implements ICodecSerializable<VaryingNumber>
{
    public abstract float getValue(float time);
}
