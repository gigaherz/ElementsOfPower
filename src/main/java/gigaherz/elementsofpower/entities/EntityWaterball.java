package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityWaterball extends EntityBallBase
{

    boolean spawnSourceBlocks;

    public EntityWaterball(World worldIn)
    {
        super(ElementsOfPower.water, worldIn);
    }

    public EntityWaterball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.water, worldIn, p_i1774_2_);
    }

    public EntityWaterball(World worldIn, double x, double y, double z)
    {
        super(ElementsOfPower.water, worldIn, x, y, z);
    }

    public EntityWaterball(World worldIn, int force, boolean spawnSourceBlocks, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.water, worldIn, force, p_i1774_2_);
        this.spawnSourceBlocks = spawnSourceBlocks;
    }

    @Override
    protected void spawnBallParticles()
    {
        for (int i = 0; i < 8; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.air)
        {
            if (spawnSourceBlocks)
            {
                worldObj.setBlockState(blockPos, Blocks.flowing_water.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 0));
            } else
            {
                worldObj.setBlockState(blockPos, Blocks.flowing_water.getDefaultState().withProperty(BlockDynamicLiquid.LEVEL, 15));
            }
        }
    }

    public int getDamageForce()
    {
        int sub = 0;
        if (worldObj.provider.doesWaterVaporize())
        {
            sub = 3;
        }
        return Math.max(super.getDamageForce() - sub, 0);
    }
}
