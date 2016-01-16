package gigaherz.elementsofpower.spells.cast.effects;

import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public abstract class SpellEffect
{
    public abstract int getColor(Spellcast cast);

    public abstract int getBeamDuration(Spellcast cast);
    public abstract int getBeamInterval(Spellcast cast);

    public int getForceModifier(Spellcast cast)
    {
        return 0;
    }

    public abstract void processDirectHit(Spellcast cast, Entity e);

    public abstract boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec);
    public abstract void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec);

    public abstract void spawnBallParticles(Spellcast cast, MovingObjectPosition mop);

    public abstract void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, int layers);

}
