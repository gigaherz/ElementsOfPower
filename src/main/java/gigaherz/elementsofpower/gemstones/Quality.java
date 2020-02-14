package gigaherz.elementsofpower.gemstones;

import net.minecraft.item.Rarity;

public enum Quality
{
    ROUGH(Rarity.COMMON, 0.9f, "elementsofpower.gemstone.quality.rough", "elementsofpower.gem_container.quality.rough"),
    COMMON(Rarity.COMMON, 1.0f, "elementsofpower.gemstone.quality.common", "elementsofpower.gem_container.quality.common"),
    SMOOTH(Rarity.UNCOMMON, 1.25f, "elementsofpower.gemstone.quality.smooth", "elementsofpower.gem_container.quality.smooth"),
    FLAWLESS(Rarity.RARE, 1.5f, "elementsofpower.gemstone.quality.flawless", "elementsofpower.gem_container.quality.flawless"),
    PURE(Rarity.EPIC, 2.0f, "elementsofpower.gemstone.quality.pure", "elementsofpower.gem_container.quality.pure");

    public static final Quality[] values = values();

    private final String translationKey;
    private final Rarity rarity;
    private final float transferSpeed;
    private final String containerTranslationKey;

    Quality(Rarity rarity, float transferSpeed, String translationKey, String containerTranslationKey)
    {
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
}
