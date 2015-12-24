package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.spells.cast.ISpellcast;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellResurrection
        extends SpellBase
{
    public MovingObjectPosition rayTracePlayer(Entity p, double blockReachDistance)
    {
        Vec3 eyePos = new Vec3(p.posX, p.posY + p.getEyeHeight(), p.posZ);
        Vec3 look = p.getLook(0);
        Vec3 targetPos = eyePos.addVector(look.xCoord * blockReachDistance, look.yCoord * blockReachDistance, look.zCoord * blockReachDistance);
        return p.worldObj.rayTraceBlocks(eyePos, targetPos, false, false, true);
    }

    @Override
    public ISpellcast getNewCast()
    {
        return new Spellcast<SpellResurrection>(this)
        {
        };
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        MovingObjectPosition pos = rayTracePlayer(player, 10);

        if (pos == null)
            return null;

        if (pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return null;

        // Resurrecting players could be done by
        // sending dimension packet or maybe respawn keeping items

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

                return getNewCast();
            }
        }

        return null;
    }
}
