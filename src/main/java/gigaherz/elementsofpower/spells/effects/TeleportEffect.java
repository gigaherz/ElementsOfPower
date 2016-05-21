package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class TeleportEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x20A020;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 0;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 0;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        if (entity == cast.player)
            return;
        entity.attackEntityFrom(DamageSource.causeThrownDamage(cast.projectile, cast.player), 0.0F);
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        if (!cast.world.isRemote)
        {
            if (cast.player instanceof EntityPlayerMP)
            {
                EntityPlayerMP playerMP = (EntityPlayerMP) cast.player;

                if (playerMP.connection.getNetworkManager().isChannelOpen()
                        && playerMP.worldObj == cast.world
                        && !playerMP.isPlayerSleeping())
                {
                    if (playerMP.isRiding())
                    {
                        playerMP.dismountRidingEntity();
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

        // Do not process the actual ball, we just want the hit target
        return false;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        for (int i = 0; i < 32; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.PORTAL,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {

    }
}
