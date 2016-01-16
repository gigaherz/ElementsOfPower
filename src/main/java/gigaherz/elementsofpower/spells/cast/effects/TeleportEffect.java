package gigaherz.elementsofpower.spells.cast.effects;

import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public class TeleportEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x20A020;
    }

    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 0;
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 0;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {
        if(e == cast.player)
            return;
        e.attackEntityFrom(DamageSource.causeThrownDamage(cast.projectile, cast.player), 0.0F);
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        if (cast.world.isRemote)
        {
            if (cast.player instanceof EntityPlayerMP)
            {
                EntityPlayerMP playerMP = (EntityPlayerMP) cast.player;

                if (playerMP.playerNetServerHandler.getNetworkManager().isChannelOpen()
                        && playerMP.worldObj == cast.world
                        && !playerMP.isPlayerSleeping())
                {
                    if (playerMP.isRiding())
                    {
                        playerMP.mountEntity(null);
                    }

                    playerMP.setPositionAndUpdate(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord);
                    playerMP.fallDistance = 0.0F;
                }
            }
            else if (cast.player != null)
            {
                cast.player.setPositionAndUpdate(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord);
                cast.player.fallDistance = 0.0F;
            }
        }

        return false;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {
        for (int i = 0; i < 32; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.PORTAL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, int layers)
    {

    }
}
