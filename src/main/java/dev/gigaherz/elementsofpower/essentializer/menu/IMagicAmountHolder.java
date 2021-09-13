package dev.gigaherz.elementsofpower.essentializer.menu;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;

public interface IMagicAmountHolder extends IMagicAmountContainer
{
    MagicAmounts getRemainingToConvert();

    void setContainedMagic(MagicAmounts contained);

    void setRemainingToConvert(MagicAmounts remaining);
}
