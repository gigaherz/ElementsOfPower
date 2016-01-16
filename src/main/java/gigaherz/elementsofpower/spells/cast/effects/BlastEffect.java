package gigaherz.elementsofpower.spells.cast.effects;

import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class BlastEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0x000000;
    }

    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 41;
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 40;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        if (!cast.world.isRemote)
        {
            boolean flag = cast.world.getGameRules().getBoolean("mobGriefing");
            cast.world.newExplosion(null, hitVec.xCoord, hitVec.yCoord, hitVec.zCoord,
                    cast.getDamageForce(), flag, flag);
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

    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, int layers)
    {

    }
}
