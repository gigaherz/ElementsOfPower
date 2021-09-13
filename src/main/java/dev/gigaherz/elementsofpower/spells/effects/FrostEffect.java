package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            Vec3 hitVec = mop.getLocation();
            cast.spawnRandomParticle(ParticleTypes.ITEM_SNOWBALL, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        if (mop != null && mop.getType() == HitResult.Type.BLOCK)
        {
            blockPos = blockPos.relative(((BlockHitResult) mop).getDirection());
            currentState = cast.world.getBlockState(blockPos);
        }

        Level world = cast.world;
        Block block = currentState.getBlock();

        int layers = (int) Math.min(1 - r, 7);

        if (block == Blocks.FIRE)
        {
            world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
        }
        else if (layers > 0)
        {
            if (block == Blocks.LAVA)
            {
                if (currentState.getValue(LiquidBlock.LEVEL) > 0)
                {
                    world.setBlockAndUpdate(blockPos, Blocks.COBBLESTONE.defaultBlockState());
                }
                else
                {
                    world.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
                }
                return;
            }
            else if (block == Blocks.WATER)
            {
                if (currentState.getValue(LiquidBlock.LEVEL) > 0)
                {
                    world.setBlockAndUpdate(blockPos, Blocks.ICE.defaultBlockState());
                }
                else
                {
                    world.setBlockAndUpdate(blockPos, Blocks.PACKED_ICE.defaultBlockState());
                }
                return;
            }
            /*else if (!Blocks.SNOW.canPlaceBlockOnSide(world, blockPos, Direction.UP))
            {
                return;
            }*/

            BlockState below = world.getBlockState(blockPos.below());
            if (below.getBlock() == Blocks.SNOW)
            {
                if (below.getValue(SnowLayerBlock.LAYERS) < 8)
                {
                    blockPos = blockPos.below();
                }
            }

            while (layers > 0)
            {
                currentState = world.getBlockState(blockPos);
                block = currentState.getBlock();

                if (block == Blocks.SNOW)
                {
                    int l = currentState.getValue(SnowLayerBlock.LAYERS);
                    if (l == 8)
                        break;
                    int add = Math.min(8 - l, layers);
                    l += add;
                    world.setBlockAndUpdate(blockPos, currentState.setValue(SnowLayerBlock.LAYERS, l));
                    layers -= add;
                }
                else if (currentState.isAir())
                {
                    int add = Math.min(8, layers);
                    world.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, add));
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
