package gigaherz.elementsofpower.spells;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class SpellResurrection
        extends SpellBase
{
    @Override
    public void castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        MovingObjectPosition pos = player.rayTrace(10, 0);

        if (pos == null)
            return;

        if (pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return;

        // Resurrecting players culd be done -- sending dimension packet or maybe respawn

        BlockPos bp = pos.getBlockPos();

        IBlockState state = world.getBlockState(bp);

        if (state.getBlock() == Blocks.dragon_egg)
        {
            EntityDragon dragon = new EntityDragon(world);

            BlockPos spawnAt = world.getTopSolidOrLiquidBlock(bp).up(5);

            dragon.setLocationAndAngles(spawnAt.getX(), spawnAt.getY(), spawnAt.getZ(), world.rand.nextFloat() * 360.0F, 0.0F);

            if (world.spawnEntityInWorld(dragon))
            {
                world.setBlockToAir(bp);
            }
        }
    }
}
