package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;

public class BeamShape extends SpellShape
{
    @Override
    public float getScale(Spellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public Spellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast)
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
            if (mop.getType() == RayTraceResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, ((EntityRayTraceResult)mop).getEntity(), mop.getHitVec());
            }
            else if (mop.getType() == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = ((BlockRayTraceResult)mop).getPos();
                BlockState state = cast.world.getBlockState(pos);
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


    public void radiate(Spellcast cast, RayTraceResult trace1, int radius)
    {
        SpellEffect effect = cast.getEffect();

        if (trace1.getType() == RayTraceResult.Type.ENTITY)
        {
            effect.processDirectHit(cast, ((EntityRayTraceResult)trace1).getEntity(), trace1.getHitVec());
        }

        effect.spawnBallParticles(cast, trace1);

        if (!effect.processEntitiesAroundBefore(cast, trace1.getHitVec()))
            return;

        if (radius > 0 && trace1.getType() == RayTraceResult.Type.BLOCK)
        {
            BlockPos bp;
            Direction facing = Direction.NORTH;
            if (trace1.getType() == RayTraceResult.Type.BLOCK)
            {
                BlockRayTraceResult brt = (BlockRayTraceResult) trace1;
                bp = brt.getPos().offset(brt.getFace());
                facing = brt.getFace();
            }
            else
            {
                bp = new BlockPos(trace1.getHitVec());
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

                        Vec3d start = trace1.getHitVec().add(new Vec3d(facing.getDirectionVec()).scale(0.5));
                        Vec3d end = new Vec3d(px + 0.5, py + 0.5, pz + 0.5);
                        BlockRayTraceResult trace2 = cast.world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, cast.player));
                        if (trace2.getType() != RayTraceResult.Type.MISS)
                            if (!trace2.getPos().equals(np))
                                continue;

                        float r = (float) Math.sqrt(r2);


                        BlockState currentState = cast.world.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, trace1.getHitVec());
    }
}