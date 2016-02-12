package gigaherz.elementsofpower.gemstones;

import net.minecraft.item.EnumRarity;

public enum Quality
{
    Rough(".rough", EnumRarity.COMMON),
    Common(".common", EnumRarity.COMMON),
    Smooth(".smooth", EnumRarity.UNCOMMON),
    Flawless(".flawless", EnumRarity.RARE),
    Pure(".pure", EnumRarity.EPIC);

    public static final Quality[] values = values();

    private final String unlocalizedName;
    private final EnumRarity rarity;

    Quality(String unlocalizedName, EnumRarity rarity)
    {
        this.unlocalizedName = unlocalizedName;
        this.rarity = rarity;
    }

    public String getUnlocalizedName()
    {
        return unlocalizedName;
    }

    public EnumRarity getRarity()
    {
        return rarity;
    }
}
