package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.spells.SpellBall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;

public class Frostball extends BallBase
{
    public Frostball(SpellBall parent)
    {
        super(parent);
    }

    @Override
    protected void spawnBallParticles(MovingObjectPosition mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            world.spawnParticle(EnumParticleTypes.SNOWBALL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.fire)
        {
            world.setBlockToAir(blockPos);
        }
        else if (layers > 0)
        {
            if (block == Blocks.flowing_lava || block == Blocks.lava)
            {
                if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.cobblestone.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.obsidian.getDefaultState());
                }
                return;
            }
            else if (block == Blocks.flowing_water || block == Blocks.water)
            {
                if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.ice.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.packed_ice.getDefaultState());
                }
                return;
            }
            else if (!Blocks.snow_layer.canPlaceBlockOnSide(world, blockPos, EnumFacing.UP))
            {
                return;
            }

            IBlockState below = world.getBlockState(blockPos.down());
            if (below.getBlock() == Blocks.snow_layer)
            {
                if (below.getValue(BlockSnow.LAYERS) < 8)
                {
                    blockPos = blockPos.down();
                }
            }

            while (layers > 0)
            {
                currentState = world.getBlockState(blockPos);
                block = currentState.getBlock();

                if (block == Blocks.snow_layer)
                {
                    int l = currentState.getValue(BlockSnow.LAYERS);
                    if (l == 8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    world.setBlockState(blockPos, currentState.withProperty(BlockSnow.LAYERS, l));
                    layers -= add;
                }
                else if (block == Blocks.air)
                {
                    int add = Math.min(8, layers);
                    world.setBlockState(blockPos, Blocks.snow_layer.getDefaultState().withProperty(BlockSnow.LAYERS, add));
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
