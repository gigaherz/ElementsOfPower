package gigaherz.elementsofpower.spells;

public enum Element
{
    Fire("fire", 1, Shape.Sphere),
    Water("water", 0, Shape.Ball),
    Air("air", 3, Shape.Cone),
    Earth("earth", 2, Shape.Ball),
    Light("light", 5, Shape.Beam),
    Darkness("darkness", 4, Shape.Beam),
    Life("life", 7, Shape.Self),
    Death("death", 6, Shape.Single);

    final int opposite;
    final Shape shape;
    final String translationName;

    public Element getOpposite()
    {
        return values[opposite];
    }

    public String translationName()
    {
        return translationName;
    }

    Element(String translationName, int opposite, Shape shape)
    {
        this.opposite = opposite;
        this.shape = shape;
        this.translationName = translationName;
    }

    public Shape getShape()
    {
        return shape;
    }

    public static final Element[] values = values();
}
