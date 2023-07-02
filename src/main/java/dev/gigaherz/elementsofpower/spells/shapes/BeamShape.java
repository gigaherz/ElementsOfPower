package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BeamShape extends SpellShape
{
    @Override
    public float getScale(InitializedSpellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast.init(player.level(), player);
    }

    @Override
    public boolean isInstant()
    {
        return false;
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        HitResult mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.getType() == HitResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, ((EntityHitResult) mop).getEntity(), mop.getLocation(), cast.player);
            }
            else if (mop.getType() == HitResult.Type.BLOCK)
            {
                BlockPos pos = ((BlockHitResult) mop).getBlockPos();
                BlockState state = cast.level.getBlockState(pos);
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

    public void radiate(InitializedSpellcast cast, HitResult trace1, int radius)
    {
        SpellEffect effect = cast.getEffect();

        if (trace1.getType() == HitResult.Type.ENTITY)
        {
            effect.processDirectHit(cast, ((EntityHitResult) trace1).getEntity(), trace1.getLocation(), cast.player);
        }

        effect.spawnBallParticles(cast, trace1);

        if (!effect.processEntitiesAroundBefore(cast, trace1.getLocation(), cast.player))
            return;

        if (radius > 0 && trace1.getType() == HitResult.Type.BLOCK)
        {
            BlockPos bp;
            Direction facing = Direction.NORTH;
            if (trace1.getType() == HitResult.Type.BLOCK)
            {
                BlockHitResult brt = (BlockHitResult) trace1;
                bp = brt.getBlockPos().relative(brt.getDirection());
                facing = brt.getDirection();
            }
            else
            {
                bp = BlockPos.containing(trace1.getLocation());
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

                        Vec3 start = trace1.getLocation().add(Vec3.atCenterOf(facing.getNormal()).scale(0.5));
                        Vec3 end = new Vec3(px + 0.5, py + 0.5, pz + 0.5);
                        BlockHitResult trace2 = cast.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cast.player));
                        if (trace2.getType() != HitResult.Type.MISS)
                            if (!trace2.getBlockPos().equals(np))
                                continue;

                        float r = (float) Math.sqrt(r2);


                        BlockState currentState = cast.level.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, trace1.getLocation(), cast.player);
    }
}