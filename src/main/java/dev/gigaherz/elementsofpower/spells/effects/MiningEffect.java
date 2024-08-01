package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
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
import net.neoforged.neoforge.common.CommonHooks;

import org.jetbrains.annotations.Nullable;

public class MiningEffect extends SpellEffect
{
    @Override
    public int getColor(SpellcastState cast)
    {
        return 0;
    }

    @Override
    public int getDuration(SpellcastState cast)
    {
        return 40 + 20 * cast.damageForce();
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 10 / cast.damageForce();
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {

    }

    @Override
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {
        Player player = cast.player();
        Level world = cast.level();
        Block block = currentState.getBlock();
        BlockState state = world.getBlockState(blockPos);

        float hardness = state.getDestroySpeed(world, blockPos);

        if (!currentState.isAir() && hardness >= 0 && hardness <= (cast.damageForce() / 3.0f))
        {
            if (player instanceof ServerPlayer serverPlayer)
            {
                ServerPlayerGameMode spgm = serverPlayer.gameMode;

                var ev = CommonHooks.fireBlockBreak(world, spgm.getGameModeForPlayer(), serverPlayer, blockPos, state);
                if (!ev.isCanceled())
                {
                    BlockEntity tileentity = world.getBlockEntity(blockPos);

                    world.levelEvent(serverPlayer, 2001, blockPos, Block.getId(currentState));

                    if (spgm.isCreative())
                    {
                        world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                    else
                    {
                        block.playerWillDestroy(world, blockPos, currentState, player);
                        boolean flag = block.onDestroyedByPlayer(state, world, blockPos, player, true, world.getFluidState(blockPos));

                        if (flag)
                        {
                            block.destroy(world, blockPos, currentState);
                            block.playerDestroy(world, player, blockPos, currentState, tileentity, cast.player().getItemInHand(InteractionHand.MAIN_HAND)); // FIXME
                        }
                    }
                }
            }
        }
    }
}
