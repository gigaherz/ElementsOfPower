package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class TeleportEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0x20A020;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 0;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 0;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec)
    {
        if (entity == cast.player)
            return;
        entity.hurt(DamageSource.thrown(cast.getProjectile(), cast.player), 0.0F);
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec)
    {
        if (!cast.world.isClientSide)
        {
            if (cast.player instanceof ServerPlayer)
            {
                ServerPlayer playerMP = (ServerPlayer) cast.player;

                if (playerMP.connection.getConnection().isConnected()
                        && playerMP.level == cast.world
                        && !playerMP.isSleeping())
                {
                    if (playerMP.isPassenger())
                    {
                        playerMP.stopRiding();
                    }

                    playerMP.teleportTo(hitVec.x, hitVec.y, hitVec.z);
                    playerMP.fallDistance = 0.0F;
                }
            }
            else if (cast.player != null)
            {
                cast.player.teleportTo(hitVec.x, hitVec.y, hitVec.z);
                cast.player.fallDistance = 0.0F;
            }
        }

        // Do not process the actual ball, we just want the hit target
        return false;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {
        for (int i = 0; i < 32; ++i)
        {
            Vec3 hitVec = mop.getLocation();
            cast.spawnRandomParticle(ParticleTypes.PORTAL, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {

    }
}
