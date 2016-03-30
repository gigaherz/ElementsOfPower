package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public abstract class SpellEffect
{
    public abstract int getColor(Spellcast cast);

    public abstract int getDuration(Spellcast cast);

    public abstract int getInterval(Spellcast cast);

    public int getForceModifier(Spellcast cast)
    {
        return 0;
    }

    public abstract void processDirectHit(Spellcast cast, Entity entity, Vec3 hitVec);

    public abstract boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec);

    public abstract void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec);

    public abstract void spawnBallParticles(Spellcast cast, MovingObjectPosition mop);

    public abstract void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float distance, MovingObjectPosition mop);
}
