package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityFrostball extends EntityBallBase
{

    public EntityFrostball(World worldIn)
    {
        super(ElementsOfPower.air, worldIn);
    }

    public EntityFrostball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.air, worldIn, p_i1774_2_);
    }

    public EntityFrostball(World worldIn, double x, double y, double z)
    {
        super(ElementsOfPower.air, worldIn, x, y, z);
    }

    public EntityFrostball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.air, worldIn, force, p_i1774_2_);
    }

    @Override
    protected void spawnBallParticles()
    {
        for (int i = 0; i < 8; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }

    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.fire)
        {
            worldObj.setBlockToAir(blockPos);
        }
        else if (layers > 0)
        {
            if (block == Blocks.flowing_lava || block == Blocks.lava)
            {
                if ((Integer) currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    worldObj.setBlockState(blockPos, Blocks.cobblestone.getDefaultState());
                }
                else
                {
                    worldObj.setBlockState(blockPos, Blocks.obsidian.getDefaultState());
                }
                return;
            }
            else if (block == Blocks.flowing_water || block == Blocks.water)
            {
                if ((Integer) currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    worldObj.setBlockState(blockPos, Blocks.ice.getDefaultState());
                }
                else
                {
                    worldObj.setBlockState(blockPos, Blocks.packed_ice.getDefaultState());
                }
                return;
            }
            else if (!Blocks.snow_layer.canPlaceBlockOnSide(worldObj, blockPos, EnumFacing.UP))
            {
                return;
            }

            while (layers > 0)
            {
                currentState = worldObj.getBlockState(blockPos);
                block = currentState.getBlock();

                if (block == Blocks.snow_layer)
                {
                    int l = (Integer) currentState.getValue(BlockSnow.LAYERS);
                    if (l == 8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    worldObj.setBlockState(blockPos, currentState.withProperty(BlockSnow.LAYERS, l));
                    layers -= add;
                }
                else if (block == Blocks.air)
                {
                    int add = Math.min(8, layers);
                    worldObj.setBlockState(blockPos, Blocks.snow_layer.getDefaultState().withProperty(BlockSnow.LAYERS, add));
                    layers -= add;
                }
                else
                {
                    break;
                }
            }
        }
    }
}
