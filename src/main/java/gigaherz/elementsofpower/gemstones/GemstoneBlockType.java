package gigaherz.elementsofpower.gemstones;

import net.minecraft.util.IStringSerializable;

public enum GemstoneBlockType implements IStringSerializable
{
    Agate("agate", Gemstone.Agate),
    Amethyst("amethyst", Gemstone.Amethyst),
    Citrine("citrine", Gemstone.Citrine),
    Ruby("ruby", Gemstone.Ruby),
    Sapphire("sapphire", Gemstone.Sapphire),
    Serendibite("serendibite", Gemstone.Serendibite);

    private final String name;
    private final Gemstone gemstone;

    GemstoneBlockType(String name, Gemstone gemstone)
    {
        this.name = name;
        this.gemstone = gemstone;
    }

    public Gemstone getGemstone()
    {
        return gemstone;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public static GemstoneBlockType[] values = values();

}