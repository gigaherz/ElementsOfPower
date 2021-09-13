package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;

public interface IMagicContainer
{
    default boolean isInfinite()
    {
        return false;
    }

    MagicAmounts getCapacity();

    void setCapacity(MagicAmounts capacity);

    MagicAmounts getContainedMagic();

    void setContainedMagic(MagicAmounts containedMagic);

    default boolean isEmpty()
    {
        if (isInfinite())
            return false;

        return getContainedMagic().isEmpty();
    }

    default boolean isFull()
    {
        if (isInfinite())
            return true;

        MagicAmounts limits = getCapacity();
        MagicAmounts amounts = getContainedMagic();

        return !amounts.lessThan(limits);
    }

    default MagicAmounts insertMagic(MagicAmounts toInsert, boolean simulate)
    {
        if (isInfinite())
            return toInsert;

        MagicAmounts contained = getContainedMagic();
        MagicAmounts availableSpace = getCapacity().subtract(contained);
        MagicAmounts potentialInsert = MagicAmounts.min(toInsert, availableSpace);
        MagicAmounts remainingAfterInsert = toInsert.subtract(potentialInsert);
        if (!simulate)
        {
            setContainedMagic(contained.add(potentialInsert));
        }
        return remainingAfterInsert;
    }

    default MagicAmounts extractMagic(MagicAmounts wanted, boolean simulate)
    {
        if (isInfinite())
            return wanted;

        MagicAmounts contained = getContainedMagic();
        MagicAmounts potentialExtract = MagicAmounts.min(contained, wanted);
        if (!simulate)
        {
            setContainedMagic(contained.subtract(potentialExtract));
        }
        return potentialExtract;
    }
}
