package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.SpellBall;
import gigaherz.elementsofpower.spells.cast.ISpellcastBall;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.Random;

public abstract class BallBase extends Spellcast<SpellBall> implements ISpellcastBall<SpellBall>
{
    protected Random rand;
    protected EntityBall projectile;

    public BallBase(SpellBall parent)
    {
        super(parent);
    }

    @Override
    public void setProjectile(EntityBall entityBall)
    {
        projectile = entityBall;
    }

    protected float getRandomForParticle()
    {
        return (rand.nextFloat() - 0.5f) * spell.getPower() / 8;
    }

    protected void processDirectHit(Entity entityHit)
    {
    }

    protected void processEntitiesAroundBefore(Vec3 hitVec)
    {
    }

    protected void processEntitiesAroundAfter(Vec3 hitVec)
    {
    }

    protected abstract void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers);

    protected abstract void spawnBallParticles(MovingObjectPosition mop);

    @Override
    public void onImpact(MovingObjectPosition mop, Random rand)
    {
        this.rand = rand;

        int force = spell.getPower();

        if (mop.entityHit != null)
        {
            processDirectHit(mop.entityHit);
        }

        spawnBallParticles(mop);

        processEntitiesAroundBefore(mop.hitVec);

        if (!world.isRemote && force > 0)
        {
            BlockPos bp = mop.getBlockPos();

            if (bp != null)
            {
                bp = bp.offset(mop.sideHit);
            }
            else
            {
                bp = new BlockPos(mop.hitVec);
            }

            int px = bp.getX();
            int py = bp.getY();
            int pz = bp.getZ();
            for (int z = pz - force; z <= pz + force; z++)
            {
                for (int x = px - force; x <= px + force; x++)
                {
                    for (int y = py - force; y <= py + force; y++)
                    {
                        float dx = Math.abs(px - x);
                        float dy = Math.abs(py - y);
                        float dz = Math.abs(pz - z);
                        float r2 = (dx * dx + dy * dy + dz * dz);
                        boolean in_sphere = r2 <= (force * force);
                        if (!in_sphere)
                            continue;

                        float r = (float) Math.sqrt(r2);

                        int layers = (int) Math.min(force - r, 7);

                        BlockPos np = new BlockPos(x, y, z);

                        IBlockState currentState = world.getBlockState(np);

                        processBlockWithinRadius(np, currentState, layers);
                    }
                }
            }
        }

        processEntitiesAroundAfter(mop.hitVec);
    }
}
