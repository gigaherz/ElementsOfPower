package gigaherz.partycool.values;

import gigaherz.partycool.ICodecSerializable;

public abstract class VaryingNumber implements ICodecSerializable<VaryingNumber>
{
    public abstract float getValue(float time);
}
