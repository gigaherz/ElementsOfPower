package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.entities.BallEntity;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BallShape extends SpellShape
{
    @Override
    public float getScale(SpellcastState cast)
    {
        return 1 + 0.25f * cast.damageForce();
    }

    @Override
    public Spellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        Level world = player.level();
        BallEntity entity = new BallEntity(world, player, cast);

        entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

        if (world.addFreshEntity(entity))
            return cast;

        return null;
    }

    @Override
    public void onImpact(SpellcastState cast, HitResult mop, Entity directEntity)
    {
        SpellEffect effect = cast.effect();

        if (mop.getType() == HitResult.Type.ENTITY)
        {
            effect.processDirectHit(cast, ((EntityHitResult) mop).getEntity(), mop.getLocation(), directEntity);
        }

        effect.spawnBallParticles(cast, mop);

        if (!effect.processEntitiesAroundBefore(cast, mop.getLocation(), directEntity))
            return;

        int force = cast.damageForce();
        if (force > 0)
        {
            BlockPos bp;
            Vec3 vec;
            Vec3 dir;
            if (mop.getType() == HitResult.Type.BLOCK)
            {
                BlockHitResult blockTrace = (BlockHitResult) mop;
                bp = blockTrace.getBlockPos();
                bp = bp.relative(blockTrace.getDirection());
                vec = Vec3.atCenterOf(bp);
                dir = Vec3.atCenterOf(blockTrace.getDirection().getNormal());
            }
            else
            {
                bp = BlockPos.containing(mop.getLocation());
                vec = mop.getLocation();
                dir = new Vec3(0, 0, 0);
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

                        BlockPos np = new BlockPos(x, y, z);

                        Vec3 start = vec.add(dir.scale(0.5));
                        Vec3 end = new Vec3(px + 0.5, py + 0.5, pz + 0.5);
                        BlockHitResult mop2 = cast.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cast.player()));
                        if (mop2.getType() != HitResult.Type.MISS)
                            if (!mop2.getBlockPos().equals(np))
                                continue;

                        float r = (float) Math.sqrt(r2);


                        BlockState currentState = cast.level().getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, mop.getLocation(), directEntity);
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }
}
