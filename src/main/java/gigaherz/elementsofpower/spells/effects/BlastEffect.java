package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;

import javax.annotation.Nullable;

public class BlastEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x000000;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 41;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 40;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        if (!cast.world.isRemote)
        {
            boolean doGriefing = cast.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            cast.world.createExplosion(null, hitVec.x, hitVec.y, hitVec.z,
                    cast.getDamageForce(), doGriefing, doGriefing ? Explosion.Mode.BREAK : Explosion.Mode.NONE);
        }

        return false;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {

    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {

    }
}
