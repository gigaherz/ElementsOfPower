package gigaherz.elementsofpower.spells;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellResurrection
        extends SpellBase
{
    @Override
    public void castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        MovingObjectPosition pos = getMovingObjectPositionFromPlayer(world, player, false);

        if (pos == null)
            return;

        //Vec1, Vec2, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock

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


    protected MovingObjectPosition getMovingObjectPositionFromPlayer(World worldIn, EntityPlayer playerIn, boolean useLiquids)
    {
        float f = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch);
        float f1 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw);
        double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX);
        double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) + (double) playerIn.getEyeHeight();
        double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ);
        Vec3 vec3 = new Vec3(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 5.0D;
        if (playerIn instanceof net.minecraft.entity.player.EntityPlayerMP)
        {
            d3 = ((net.minecraft.entity.player.EntityPlayerMP) playerIn).theItemInWorldManager.getBlockReachDistance();
        }
        Vec3 vec31 = vec3.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
        return worldIn.rayTraceBlocks(vec3, vec31, useLiquids, !useLiquids, false);
    }

}
