package gigaherz.elementsofpower.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSnow;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EntityFrostball extends EntityBallBase {

    public EntityFrostball(World worldIn)
    {
        super(worldIn);
    }
    public EntityFrostball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
    }
    public EntityFrostball(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }
    public EntityFrostball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, force, p_i1774_2_);
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

        if(block == Blocks.fire)
        {
            worldObj.setBlockToAir(blockPos);
        }
        else if (layers > 0)
        {
            if(block == Blocks.lava)
            {
                worldObj.setBlockState(blockPos,Blocks.obsidian.getDefaultState(), 2);
                return;
            }
            else if(block == Blocks.flowing_lava)
            {
                worldObj.setBlockState(blockPos,Blocks.cobblestone.getDefaultState(), 2);
                return;
            }
            else if (block == Blocks.flowing_water || block == Blocks.water)
            {
                if((Integer)currentState.getValue(BlockDynamicLiquid.LEVEL) > 0) {
                    worldObj.setBlockState(blockPos, Blocks.ice.getDefaultState(), 2);
                } else
                {
                    worldObj.setBlockState(blockPos, Blocks.packed_ice.getDefaultState(), 2);
                }
                return;
            }
            else if(!Blocks.snow_layer.canPlaceBlockOnSide(worldObj, blockPos, EnumFacing.UP))
            {
                return;
            }

            while(layers > 0)
            {
                currentState = worldObj.getBlockState(blockPos);
                block = currentState.getBlock();

                if (block == Blocks.snow_layer) {
                    int l = (Integer) currentState.getValue(BlockSnow.LAYERS);
                    if(l==8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    worldObj.setBlockState(blockPos, currentState.withProperty(BlockSnow.LAYERS, l), 2);
                    layers -= add;
                } else if (block == Blocks.air) {
                    int add = Math.min(8, layers);
                    worldObj.setBlockState(blockPos, Blocks.snow_layer.getDefaultState().withProperty(BlockSnow.LAYERS, add), 2);
                    layers -= add;
                } else {
                    break;
                }
            }
        }
    }
}
