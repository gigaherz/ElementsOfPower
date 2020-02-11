package gigaherz.elementsofpower.gemstones;

import net.minecraft.block.Block;

public class BlockGemstone extends Block
{
    private final Gemstone type;

    public BlockGemstone(Gemstone type, Properties properties)
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
