package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

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
            boolean flag = cast.world.getGameRules().getBoolean("mobGriefing");
            cast.world.newExplosion(null, hitVec.xCoord, hitVec.yCoord, hitVec.zCoord,
                    cast.getDamageForce(), flag, flag);
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
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {

    }
}
