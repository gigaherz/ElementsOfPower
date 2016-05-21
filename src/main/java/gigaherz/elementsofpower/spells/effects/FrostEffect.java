package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FrostEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0xFF8080;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 8;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.SNOWBALL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        World world = cast.world;
        Block block = currentState.getBlock();

        int layers = (int) Math.min(1 - r, 7);

        if (block == Blocks.FIRE)
        {
            world.setBlockToAir(blockPos);
        }
        else if (layers > 0)
        {
            if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
            {
                if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.COBBLESTONE.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState());
                }
                return;
            }
            else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
            {
                if (currentState.getValue(BlockDynamicLiquid.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.PACKED_ICE.getDefaultState());
                }
                return;
            }
            else if (!Blocks.SNOW_LAYER.canPlaceBlockOnSide(world, blockPos, EnumFacing.UP))
            {
                return;
            }

            IBlockState below = world.getBlockState(blockPos.down());
            if (below.getBlock() == Blocks.SNOW_LAYER)
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

                if (block == Blocks.SNOW_LAYER)
                {
                    int l = currentState.getValue(BlockSnow.LAYERS);
                    if (l == 8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    world.setBlockState(blockPos, currentState.withProperty(BlockSnow.LAYERS, l));
                    layers -= add;
                }
                else if (block == Blocks.AIR)
                {
                    int add = Math.min(8, layers);
                    world.setBlockState(blockPos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, add));
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
