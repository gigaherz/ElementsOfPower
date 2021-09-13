package dev.gigaherz.elementsofpower.spelldust;

import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.world.item.Item;

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
