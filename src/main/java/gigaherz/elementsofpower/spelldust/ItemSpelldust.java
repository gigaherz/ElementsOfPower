package gigaherz.elementsofpower.spelldust;

import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.item.Item;

public class ItemSpelldust extends Item
{
    private final Gemstone type;

    public ItemSpelldust(Gemstone type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    public Gemstone getType()
    {
        return type;
    }
}
