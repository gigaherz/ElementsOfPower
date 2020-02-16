package gigaherz.elementsofpower.gemstones;

import net.minecraft.item.Rarity;

import javax.annotation.Nullable;

public enum Quality
{
    ROUGH(0, Rarity.COMMON, 0.9f, "elementsofpower.gemstone.quality.rough", "elementsofpower.gem_container.quality.rough"),
    COMMON(1, Rarity.COMMON, 1.0f, "elementsofpower.gemstone.quality.common", "elementsofpower.gem_container.quality.common"),
    SMOOTH(2, Rarity.UNCOMMON, 1.25f, "elementsofpower.gemstone.quality.smooth", "elementsofpower.gem_container.quality.smooth"),
    FLAWLESS(3, Rarity.RARE, 1.5f, "elementsofpower.gemstone.quality.flawless", "elementsofpower.gem_container.quality.flawless"),
    PURE(4, Rarity.EPIC, 2.0f, "elementsofpower.gemstone.quality.pure", "elementsofpower.gem_container.quality.pure");

    public static final Quality[] values = values();

    private final int index;
    private final String translationKey;
    private final Rarity rarity;
    private final float transferSpeed;
    private final String containerTranslationKey;

    Quality(int index, Rarity rarity, float transferSpeed, String translationKey, String containerTranslationKey)
    {
        this.index = index;
        this.translationKey = translationKey;
        this.rarity = rarity;
        this.transferSpeed = transferSpeed;
        this.containerTranslationKey = containerTranslationKey;
    }

    public String getTranslationKey()
    {
        return translationKey;
    }

    public Rarity getRarity()
    {
        return rarity;
    }

    public float getTransferSpeed()
    {
        return transferSpeed;
    }

    public String getContainerTranslationKey()
    {
        return containerTranslationKey;
    }

    public int getIndex()
    {
        return index;
    }

    @Nullable
    public static Quality byIndex(int index)
    {
        for(Quality q : values)
            if (q.index == index)
                return q;

        return null;
    }
}
