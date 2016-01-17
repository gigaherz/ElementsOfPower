package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class MiningEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0;
    }

    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 40 + 20 * cast.getDamageForce();
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 10 / cast.getDamageForce();
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

    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, MovingObjectPosition mop)
    {
        EntityPlayer player = cast.player;
        World world = cast.world;
        Block block = currentState.getBlock();

        float hardness = block.getBlockHardness(world, blockPos);

        if (!block.isAir(world, blockPos) && hardness >= 0 && hardness <= (cast.getDamageForce() / 3.0f))
        {
            if (player instanceof EntityPlayerMP)
            {
                EntityPlayerMP playermp = (EntityPlayerMP) player;
                ItemInWorldManager mgr = playermp.theItemInWorldManager;

                int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, mgr.getGameType(), playermp, blockPos);
                if (exp != -1)
                {
                    TileEntity tileentity = world.getTileEntity(blockPos);

                    world.playAuxSFXAtEntity(playermp, 2001, blockPos, Block.getStateId(currentState));

                    if (mgr.isCreative())
                    {
                        world.setBlockToAir(blockPos);
                    }
                    else
                    {
                        block.onBlockHarvested(world, blockPos, currentState, player);
                        boolean flag = block.removedByPlayer(world, blockPos, player, true);

                        if (flag)
                        {
                            block.onBlockDestroyedByPlayer(world, blockPos, currentState);
                            block.harvestBlock(world, player, blockPos, currentState, tileentity);
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
