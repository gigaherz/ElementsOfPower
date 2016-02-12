package gigaherz.elementsofpower.gemstones;

public enum Gemstone
{
    Ruby(Element.Fire, "ruby"), // red
    Sapphire(Element.Water, "sapphire"), // blue
    Citrine(Element.Air, "citrine"), // yellow
    Agate(Element.Earth, "agate"), // brown
    Quartz(Element.Light, "quartz"), // white
    Serendibite(Element.Darkness, "serendibite"), // black
    Emerald(Element.Life, "emerald"), // green
    Amethyst(Element.Death, "amethyst"), // purple

    Diamond(null, "diamond"); // clear

    private final Element element;
    private final String name;
    private final String unlocalizedName;

    Gemstone(Element element, String name)
    {
        this.element = element;
        this.name = name;
        this.unlocalizedName = "." + name;
    }

    public Element getElement()
    {
        return element;
    }

    public String getUnlocalizedName()
    {
        return unlocalizedName;
    }

    public String getName()
    {
        return name;
    }

    public static final Gemstone[] values = values();
}
