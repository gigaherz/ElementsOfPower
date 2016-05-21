package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MiningEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 40 + 20 * cast.getDamageForce();
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 10 / cast.getDamageForce();
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

    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        EntityPlayer player = cast.player;
        World world = cast.world;
        Block block = currentState.getBlock();
        IBlockState state = world.getBlockState(blockPos);

        float hardness = state.getBlockHardness(world, blockPos);

        if (!block.isAir(state, world, blockPos) && hardness >= 0 && hardness <= (cast.getDamageForce() / 3.0f))
        {
            if (player instanceof EntityPlayerMP)
            {
                EntityPlayerMP playermp = (EntityPlayerMP) player;
                PlayerInteractionManager mgr = playermp.interactionManager;

                int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, mgr.getGameType(), playermp, blockPos);
                if (exp != -1)
                {
                    TileEntity tileentity = world.getTileEntity(blockPos);

                    world.playEvent(playermp, 2001, blockPos, Block.getStateId(currentState));

                    if (mgr.isCreative())
                    {
                        world.setBlockToAir(blockPos);
                    }
                    else
                    {
                        block.onBlockHarvested(world, blockPos, currentState, player);
                        boolean flag = block.removedByPlayer(state, world, blockPos, player, true);

                        if (flag)
                        {
                            block.onBlockDestroyedByPlayer(world, blockPos, currentState);
                            block.harvestBlock(world, player, blockPos, currentState, tileentity, cast.getCastingPlayer().getHeldItem(EnumHand.MAIN_HAND)); // FIXME
                        }

                        // Drop experiance
                        if (!mgr.isCreative() && exp > 0)
                        {
                            block.dropXpOnBlockBreak(world, blockPos, exp);
                        }
                    }
                }
            }
        }
    }
}
