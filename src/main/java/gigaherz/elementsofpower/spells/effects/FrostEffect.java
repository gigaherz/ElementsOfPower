package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FrostEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0xFF8080;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 8;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vector3d hitVec)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vector3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vector3d hitVec)
    {

    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, RayTraceResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            Vector3d hitVec = mop.getHitVec();
            cast.spawnRandomParticle(ParticleTypes.ITEM_SNOWBALL, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null && mop.getType() == RayTraceResult.Type.BLOCK)
        {
            blockPos = blockPos.offset(((BlockRayTraceResult) mop).getFace());
            currentState = cast.world.getBlockState(blockPos);
        }

        World world = cast.world;
        Block block = currentState.getBlock();

        int layers = (int) Math.min(1 - r, 7);

        if (block == Blocks.FIRE)
        {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
        }
        else if (layers > 0)
        {
            if (block == Blocks.LAVA)
            {
                if (currentState.get(FlowingFluidBlock.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.COBBLESTONE.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState());
                }
                return;
            }
            else if (block == Blocks.WATER)
            {
                if (currentState.get(FlowingFluidBlock.LEVEL) > 0)
                {
                    world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
                }
                else
                {
                    world.setBlockState(blockPos, Blocks.PACKED_ICE.getDefaultState());
                }
                return;
            }
            /*else if (!Blocks.SNOW.canPlaceBlockOnSide(world, blockPos, Direction.UP))
            {
                return;
            }*/

            BlockState below = world.getBlockState(blockPos.down());
            if (below.getBlock() == Blocks.SNOW)
            {
                if (below.get(SnowBlock.LAYERS) < 8)
                {
                    blockPos = blockPos.down();
                }
            }

            while (layers > 0)
            {
                currentState = world.getBlockState(blockPos);
                block = currentState.getBlock();

                if (block == Blocks.SNOW)
                {
                    int l = currentState.get(SnowBlock.LAYERS);
                    if (l == 8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    world.setBlockState(blockPos, currentState.with(SnowBlock.LAYERS, l));
                    layers -= add;
                }
                else if (currentState.isAir(cast.world, blockPos))
                {
                    int add = Math.min(8, layers);
                    world.setBlockState(blockPos, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, add));
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
