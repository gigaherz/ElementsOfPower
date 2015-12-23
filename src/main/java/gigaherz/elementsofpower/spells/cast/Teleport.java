package gigaherz.elementsofpower.spells.cast;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.SpellTeleport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

public class Teleport extends Spellcast<SpellTeleport> implements ISpellcastBall<SpellTeleport>
{
    EntityBall projectile;

    public Teleport(SpellTeleport parent)
    {
        super(parent);
    }

    @Override
    public void setProjectile(EntityBall entityBall)
    {
        projectile = entityBall;
    }

    public void onImpact(MovingObjectPosition mop, Random rand)
    {
        if (mop.entityHit != null)
        {
            if (mop.entityHit == player)
            {
                return;
            }

            mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(projectile, player), 0.0F);
        }

        for (int i = 0; i < 32; ++i)
        {
            world.spawnParticle(EnumParticleTypes.PORTAL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord + rand.nextDouble() * 2.0D, mop.hitVec.zCoord,
                    rand.nextGaussian(), 0.0D, rand.nextGaussian());
        }

        if (world.isRemote)
        {
            if (player instanceof EntityPlayerMP)
            {
                EntityPlayerMP playerMP = (EntityPlayerMP)this.player;

                if (playerMP.playerNetServerHandler.getNetworkManager().isChannelOpen()
                        && playerMP.worldObj == world
                        && !playerMP.isPlayerSleeping())
                {
                    if (playerMP.isRiding())
                    {
                        playerMP.mountEntity(null);
                    }

                    playerMP.setPositionAndUpdate(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
                    playerMP.fallDistance = 0.0F;
                }
            }
            else if (player != null)
            {
                player.setPositionAndUpdate(mop.hitVec.xCoord,mop.hitVec.yCoord,mop.hitVec.zCoord);
                player.fallDistance = 0.0F;
            }
        }
    }
}
