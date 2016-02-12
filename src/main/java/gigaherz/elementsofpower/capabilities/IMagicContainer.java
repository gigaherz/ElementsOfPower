package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.database.MagicAmounts;

public interface IMagicContainer
{
    MagicAmounts getCapacity();

    MagicAmounts getContainedMagic();

    void setContainedMagic(MagicAmounts containedMagic);
}
