package gigaherz.elementsofpower.gemstones;

public enum Element
{
    Fire(1, Shape.Sphere),
    Water(0, Shape.Ball),
    Air(3, Shape.Cone),
    Earth(2, Shape.Ball),
    Light(5, Shape.Beam),
    Darkness(4, Shape.Beam),
    Life(7, Shape.Self),
    Death(6, Shape.Single);

    final int opposite;
    final Shape shape;

    public Element getOpposite()
    {
        return values[opposite];
    }

    Element(int opposite, Shape shape)
    {
        this.opposite = opposite;
        this.shape = shape;
    }

    public Shape getShape()
    {
        return shape;
    }

    public static final Element[] values = values();
}
