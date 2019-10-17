package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class BeamShape extends SpellShape
{
    @Override
    public float getScale(Spellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
    {
        return cast;
    }

    @Override
    public boolean isInstant()
    {
        return false;
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        RayTraceResult mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.typeOfHit == RayTraceResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, mop.entityHit, mop.hitVec);
            }
            else if (mop.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = mop.getBlockPos();
                IBlockState state = cast.world.getBlockState(pos);
                if (cast.getRadiating() > 0)
                {
                    radiate(cast, mop, cast.getRadiating());
                }
                else
                {
                    cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
                }
            }
        }
    }


    public void radiate(Spellcast cast, RayTraceResult mop, int radius)
    {
        SpellEffect effect = cast.getEffect();

        if (mop.entityHit != null)
        {
            effect.processDirectHit(cast, mop.entityHit, mop.hitVec);
        }

        effect.spawnBallParticles(cast, mop);

        if (!effect.processEntitiesAroundBefore(cast, mop.hitVec))
            return;

        if (radius > 0)
        {
            BlockPos bp = mop.getBlockPos();

            if (mop.typeOfHit == RayTraceResult.Type.BLOCK)
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
            for (int z = pz - radius; z <= pz + radius; z++)
            {
                for (int x = px - radius; x <= px + radius; x++)
                {
                    for (int y = py - radius; y <= py + radius; y++)
                    {
                        float dx = Math.abs(px - x);
                        float dy = Math.abs(py - y);
                        float dz = Math.abs(pz - z);
                        float r2 = (dx * dx + dy * dy + dz * dz);
                        boolean in_sphere = r2 <= (radius * radius);
                        if (!in_sphere)
                            continue;

                        BlockPos np = new BlockPos(x, y, z);

                        RayTraceResult mop2 = cast.world.rayTraceBlocks(
                                mop.hitVec.add(new Vec3d(mop.sideHit.getDirectionVec()).scale(0.5)),
                                new Vec3d(px + 0.5, py + 0.5, pz + 0.5), false, true, false);
                        if (mop2 != null && mop2.typeOfHit != RayTraceResult.Type.MISS)
                            if (!mop2.getBlockPos().equals(np))
                                continue;

                        float r = (float) Math.sqrt(r2);


                        IBlockState currentState = cast.world.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, mop.hitVec);
    }
}