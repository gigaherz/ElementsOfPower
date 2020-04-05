package gigaherz.elementsofpower.gemstones;

import net.minecraft.block.OreBlock;

public class GemstoneOreBlock extends OreBlock
{
    private final Gemstone type;

    public GemstoneOreBlock(Gemstone type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    public Gemstone getType()
    {
        return type;
    }
}
