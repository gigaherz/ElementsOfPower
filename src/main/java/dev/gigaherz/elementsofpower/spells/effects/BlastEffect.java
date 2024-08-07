package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class BlastEffect extends SpellEffect
{
    @Override
    public int getColor(SpellcastState cast)
    {
        return 0x000000;
    }

    @Override
    public int getDuration(SpellcastState cast)
    {
        return 41;
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 40;
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        if (!cast.level().isClientSide)
        {
            boolean doGriefing = cast.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            cast.level().explode(cast.player(), hitVec.x, hitVec.y, hitVec.z,
                    cast.damageForce(), doGriefing, doGriefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        }

        return false;
    }

    @Override
    public void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {

    }

    @Override
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float r, @Nullable HitResult mop)
    {

    }
}
