package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.database.MagicAmounts;

public interface IMagicAmountHolder extends IMagicAmountContainer
{
    MagicAmounts getRemainingToConvert();

    void setContainedMagic(MagicAmounts contained);

    void setRemainingToConvert(MagicAmounts remaining);
}
