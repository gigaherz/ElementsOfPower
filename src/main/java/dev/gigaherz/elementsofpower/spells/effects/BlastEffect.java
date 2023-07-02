package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class BlastEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0x000000;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 41;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 40;
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {
        if (!cast.level.isClientSide)
        {
            boolean doGriefing = cast.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            cast.level.explode(cast.player, hitVec.x, hitVec.y, hitVec.z,
                    cast.getDamageForce(), doGriefing, doGriefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        }

        return false;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, HitResult mop)
    {

    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {

    }
}
