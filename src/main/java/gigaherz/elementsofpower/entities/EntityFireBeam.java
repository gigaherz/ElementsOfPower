package gigaherz.elementsofpower.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityFireBeam extends EntityBeamBase
{
    public EntityFireBeam(World worldIn)
    {
        super(worldIn);
    }

    public EntityFireBeam(World worldIn, EntityLivingBase caster, float maxDistance, int power, int effectInterval, int timeToLive)
    {
        super(worldIn, caster, maxDistance, power, effectInterval, timeToLive);
    }

    protected void applyEffect()
    {
        if(hitInfo == null)
            return;

        if(hitInfo.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            BlockPos pos = hitInfo.getBlockPos().offset(hitInfo.sideHit);
            if(worldObj.getBlockState(pos).getBlock() == Blocks.air)
            {
                worldObj.setBlockState(pos, Blocks.fire.getDefaultState());
            }
        }
        else if(hitInfo.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
        {
            this.func_174815_a(this.getCaster(), hitInfo.entityHit);

            if (!hitInfo.entityHit.isImmuneToFire())
            {
                hitInfo.entityHit.setFire(power);
            }
        }
    }
}
