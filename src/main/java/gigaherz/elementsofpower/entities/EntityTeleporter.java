package gigaherz.elementsofpower.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityTeleporter extends EntityBallBase
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
    public int getBallColor()
    {
        return 0x008000;
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

    @Override
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

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {

    }

    @Override
    protected void spawnBallParticles()
    {

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
