package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class MiningEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 40 + 20 * cast.getDamageForce();
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 10 / cast.getDamageForce();
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

    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        PlayerEntity player = cast.player;
        World world = cast.world;
        Block block = currentState.getBlock();
        BlockState state = world.getBlockState(blockPos);

        float hardness = state.getBlockHardness(world, blockPos);

        if (!block.isAir(state, world, blockPos) && hardness >= 0 && hardness <= (cast.getDamageForce() / 3.0f))
        {
            if (player instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity playermp = (ServerPlayerEntity) player;
                PlayerInteractionManager mgr = playermp.interactionManager;

                int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, mgr.getGameType(), playermp, blockPos);
                if (exp != -1)
                {
                    TileEntity tileentity = world.getTileEntity(blockPos);

                    world.playEvent(playermp, 2001, blockPos, Block.getStateId(currentState));

                    if (mgr.isCreative())
                    {
                        world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                    }
                    else
                    {
                        block.onBlockHarvested(world, blockPos, currentState, player);
                        boolean flag = block.removedByPlayer(state, world, blockPos, player, true, world.getFluidState(blockPos));

                        if (flag)
                        {
                            block.onPlayerDestroy(world, blockPos, currentState);
                            block.harvestBlock(world, player, blockPos, currentState, tileentity, cast.getCastingPlayer().getHeldItem(Hand.MAIN_HAND)); // FIXME
                        }

                        // Drop experiance
                        if (!mgr.isCreative() && exp > 0)
                        {
                            block.dropXpOnBlockBreak((ServerWorld)world, blockPos, exp);
                        }
                    }
                }
            }
        }
    }
}
