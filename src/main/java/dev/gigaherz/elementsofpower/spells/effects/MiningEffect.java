package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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

    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        Player player = cast.player;
        Level world = cast.world;
        Block block = currentState.getBlock();
        BlockState state = world.getBlockState(blockPos);

        float hardness = state.getDestroySpeed(world, blockPos);

        if (!currentState.isAir() && hardness >= 0 && hardness <= (cast.getDamageForce() / 3.0f))
        {
            if (player instanceof ServerPlayer)
            {
                ServerPlayer playermp = (ServerPlayer) player;
                ServerPlayerGameMode mgr = playermp.gameMode;

                int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, mgr.getGameModeForPlayer(), playermp, blockPos);
                if (exp != -1)
                {
                    BlockEntity tileentity = world.getBlockEntity(blockPos);

                    world.levelEvent(playermp, 2001, blockPos, Block.getId(currentState));

                    if (mgr.isCreative())
                    {
                        world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                    else
                    {
                        block.playerWillDestroy(world, blockPos, currentState, player);
                        boolean flag = block.removedByPlayer(state, world, blockPos, player, true, world.getFluidState(blockPos));

                        if (flag)
                        {
                            block.destroy(world, blockPos, currentState);
                            block.playerDestroy(world, player, blockPos, currentState, tileentity, cast.getCastingPlayer().getItemInHand(InteractionHand.MAIN_HAND)); // FIXME
                        }

                        // Drop experiance
                        if (!mgr.isCreative() && exp > 0)
                        {
                            block.popExperience((ServerLevel) world, blockPos, exp);
                        }
                    }
                }
            }
        }
    }
}
