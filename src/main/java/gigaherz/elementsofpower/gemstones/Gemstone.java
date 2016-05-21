package gigaherz.elementsofpower.gemstones;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nullable;

public enum Gemstone implements IStringSerializable
{
    Ruby(Element.Fire, "ruby", 0xFFFF0000), // red
    Sapphire(Element.Water, "sapphire", 0xFF0000FF), // blue
    Citrine(Element.Air, "citrine", 0xFFFFFF00), // yellow
    Agate(Element.Earth, "agate", 0xFF7F3F00), // brown
    Quartz(Element.Light, "quartz", 0xFFFFFFFF), // white
    Serendibite(Element.Darkness, "serendibite", 0xFF0F0F0F), // black
    Emerald(Element.Life, "emerald", 0xFF00FF00), // green
    Amethyst(Element.Death, "amethyst", 0xFFAF00FF), // purple

    Diamond(null, "diamond", 0xFF7FFFCF), // clear

    Creativite(null, "creative", 0xFF000000);

    private final Element element;
    private final String name;
    private final String unlocalizedName;
    private final int tintColor;

    Gemstone(Element element, String name, int tintColor)
    {
        this.element = element;
        this.name = name;
        this.unlocalizedName = "." + name;
        this.tintColor = tintColor;
    }

    @Nullable
    public Element getElement()
    {
        return element;
    }

    public String getUnlocalizedName()
    {
        return unlocalizedName;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public int getTintColor()
    {
        return tintColor;
    }

    public static final Gemstone[] values = values();

}
