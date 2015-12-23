package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.spells.cast.Teleport;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityTeleporter extends EntityBall
{
    @SuppressWarnings("unused")
    public EntityTeleporter(World worldIn)
    {
        super(worldIn);
    }

    public EntityTeleporter(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
    }

    @SuppressWarnings("unused")
    public EntityTeleporter(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    @Override
    protected float getVelocity()
    {
        return 2.0F;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

    Teleport spellcast;

    @Override
    protected void onImpact(MovingObjectPosition pos)
    {
        spellcast.onImpact(pos, rand);

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }

    public void onUpdate()
    {
        EntityLivingBase thrower = this.getThrower();

        if (thrower != null && thrower instanceof EntityPlayer && !thrower.isEntityAlive())
        {
            this.setDead();
        }
        else
        {
            super.onUpdate();
        }
    }
}
