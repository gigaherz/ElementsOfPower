package gigaherz.elementsofpower.spelldust;

import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.item.Item;

public class SpelldustItem extends Item
{
    private final Gemstone type;

    public SpelldustItem(Gemstone type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    public Gemstone getType()
    {
        return type;
    }
}
