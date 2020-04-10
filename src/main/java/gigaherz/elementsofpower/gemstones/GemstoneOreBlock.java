package gigaherz.elementsofpower.gemstones;

import net.minecraft.block.OreBlock;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

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

    @Override
    protected int getExperience(Random p_220281_1_) {
        return MathHelper.nextInt(p_220281_1_, 3, 7);
    }
}
