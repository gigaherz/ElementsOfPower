package dev.gigaherz.elementsofpower.gemstones;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.OreBlock;

public class GemstoneOreBlock extends OreBlock
{
    private final Gemstone type;

    public GemstoneOreBlock(Gemstone type, Properties properties)
    {
        super(properties, UniformInt.of(3, 7));
        this.type = type;
    }

    public Gemstone getType()
    {
        return type;
    }
}
