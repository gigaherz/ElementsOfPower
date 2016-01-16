package gigaherz.elementsofpower.spells.cast.shapes;

import gigaherz.elementsofpower.spells.SpellBall;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import gigaherz.elementsofpower.spells.cast.effects.SpellEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

public class SpellcastBall extends Spellcast<SpellBall>
{
    protected Random rand;

    public SpellcastBall(SpellBall parent, SpellEffect effect)
    {
        super(parent, effect);
    }

    public float getRandomForParticle()
    {
        return (rand.nextFloat() - 0.5f) * spell.getPower() / 8;
    }

    public void onImpact(MovingObjectPosition mop, Random rand)
    {
        this.rand = rand;

        if (mop.entityHit != null)
        {
            effect.processDirectHit(this, mop.entityHit);
        }

        effect.spawnBallParticles(this, mop);

        if (!effect.processEntitiesAroundBefore(this, mop.hitVec))
            return;

        int force = getDamageForce();
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

                        effect.processBlockWithinRadius(this, np, currentState, layers);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(this, mop.hitVec);
    }
}
