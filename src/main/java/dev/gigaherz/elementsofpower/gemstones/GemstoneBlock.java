package dev.gigaherz.elementsofpower.gemstones;

import net.minecraft.world.level.block.Block;

public class GemstoneBlock extends Block
{
    private final Gemstone type;

    public GemstoneBlock(Gemstone type, Properties properties)
    {
        super(properties);
        this.type = type;
    }

    public Gemstone getType()
    {
        return type;
    }

    //@Override
    //public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon)
    //{
    //    return true;
    //}
}

