package gigaherz.elementsofpower.gemstones;

import net.minecraft.item.EnumRarity;

public enum Quality
{
    Rough(".rough", EnumRarity.COMMON, 0.9f),
    Common(".common", EnumRarity.COMMON, 1.0f),
    Smooth(".smooth", EnumRarity.UNCOMMON, 1.25f),
    Flawless(".flawless", EnumRarity.RARE, 1.5f),
    Pure(".pure", EnumRarity.EPIC, 2.0f);

    public static final Quality[] values = values();

    private final String unlocalizedName;
    private final EnumRarity rarity;
    private final float transferSpeed;

    Quality(String unlocalizedName, EnumRarity rarity, float transferSpeed)
    {
        this.unlocalizedName = unlocalizedName;
        this.rarity = rarity;
        this.transferSpeed = transferSpeed;
    }

    public String getUnlocalizedName()
    {
        return unlocalizedName;
    }

    public EnumRarity getRarity()
    {
        return rarity;
    }

    public float getTransferSpeed()
    {
        return transferSpeed;
    }
}
