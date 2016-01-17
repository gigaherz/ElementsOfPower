package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class FrostEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0xFF8080;
    }

    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 8;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.SNOWBALL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, MovingObjectPosition mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        World world = cast.world;
        Block block = currentState.getBlock();

        int layers = (int) Math.min(1 - r, 7);

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
