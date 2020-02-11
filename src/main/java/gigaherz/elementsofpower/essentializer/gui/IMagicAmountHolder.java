package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.database.MagicAmounts;

public interface IMagicAmountHolder
{
    MagicAmounts getContainedMagic();

    MagicAmounts getRemainingToConvert();

    void setContainedMagic(MagicAmounts contained);

    void setRemainingToConvert(MagicAmounts remaining);
}
