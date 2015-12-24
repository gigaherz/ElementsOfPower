package gigaherz.elementsofpower.spells.cast.beams;

import gigaherz.elementsofpower.spells.SpellBeam;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class Miningbeam extends BeamBase
{
    public Miningbeam(SpellBeam spell)
    {
        super(spell);
    }

    @Override
    protected void applyEffect(MovingObjectPosition mop)
    {
        if (mop == null)
            return;

        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            BlockPos pos = mop.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            float hardness = block.getBlockHardness(world, pos);

            if (!block.isAir(world, pos) && hardness >= 0 && hardness <= (spell.getPower() / 3.0f))
            {
                if (player instanceof EntityPlayerMP)
                {
                    EntityPlayerMP playermp = (EntityPlayerMP) player;
                    ItemInWorldManager mgr = playermp.theItemInWorldManager;

                    int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, mgr.getGameType(), playermp, pos);
                    if (exp != -1)
                    {
                        TileEntity tileentity = world.getTileEntity(pos);

                        world.playAuxSFXAtEntity(playermp, 2001, pos, Block.getStateId(state));

                        if (mgr.isCreative())
                        {
                            world.setBlockToAir(pos);
                        }
                        else
                        {
                            block.onBlockHarvested(world, pos, state, player);
                            boolean flag = block.removedByPlayer(world, pos, player, true);

                            if (flag)
                            {
                                block.onBlockDestroyedByPlayer(world, pos, state);
                                block.harvestBlock(world, player, pos, state, tileentity);
                            }

                            // Drop experiance
                            if (!mgr.isCreative() && exp > 0)
                            {
                                block.dropXpOnBlockBreak(world, pos, exp);
                            }
                        }
                    }
                }
            }
        }
        else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
        {
            // TODO: this.applyEnchantments(this.getCaster(), hitInfo.entityHit);

            if (!mop.entityHit.isImmuneToFire())
            {
                mop.entityHit.setFire(spell.getPower());
            }
        }
    }
}
