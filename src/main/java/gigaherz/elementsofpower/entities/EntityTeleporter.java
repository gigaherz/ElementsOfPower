package gigaherz.elementsofpower.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityTeleporter extends EntityThrowable implements IRenderStackProvider
{
    private ItemStack stackForRendering = new ItemStack(Items.ender_pearl, 1);

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
    public ItemStack getStackForRendering()
    {
        return stackForRendering;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition hitInfo)
    {
        EntityLivingBase thrower = this.getThrower();

        if (hitInfo.entityHit != null)
        {
            if (hitInfo.entityHit == thrower)
            {
                return;
            }

            hitInfo.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), 0.0F);
        }

        for (int i = 0; i < 32; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL,
                    this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ,
                    this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian());
        }

        if (!this.worldObj.isRemote)
        {
            if (thrower instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP)thrower;

                if (player.playerNetServerHandler.getNetworkManager().isChannelOpen()
                        && player.worldObj == this.worldObj
                        && !player.isPlayerSleeping())
                {
                    if (player.isRiding())
                    {
                        player.mountEntity(null);
                    }

                    player.setPositionAndUpdate(hitInfo.hitVec.xCoord, hitInfo.hitVec.yCoord, hitInfo.hitVec.zCoord);
                    player.fallDistance = 0.0F;
                }
            }
            else if (thrower != null)
            {
                thrower.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                thrower.fallDistance = 0.0F;
            }

            this.setDead();
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
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
