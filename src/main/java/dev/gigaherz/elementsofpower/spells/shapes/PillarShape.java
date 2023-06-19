package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PillarShape extends SpellShape
{
    @Override
    public float getScale(InitializedSpellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    private boolean createSpellEntity(LivingEntity caster, double posX, double posZ, double minY, double maxY, float yaw, int delayTicks) {
        BlockPos blockpos = BlockPos.containing(posX, maxY, posZ);
        boolean flag = false;
        double d0 = 0.0D;

        do {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = caster.level().getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(caster.level(), blockpos1, Direction.UP)) {
                if (!caster.level().isEmptyBlock(blockpos)) {
                    BlockState blockstate1 = caster.level().getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(caster.level(), blockpos);
                    if (!voxelshape.isEmpty()) {
                        d0 = voxelshape.max(Direction.Axis.Y);
                    }
                }

                flag = true;
                break;
            }

            blockpos = blockpos.below();
        } while(blockpos.getY() >= Mth.floor(minY) - 1);

        if (flag) {
            return caster.level().addFreshEntity(new EvokerFangs(caster.level(), posX, (double)blockpos.getY() + d0, posZ, yaw, delayTicks, caster));
        }

        return false;
    }

    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        InitializedSpellcast spellcast = cast.init(player.level(), player);

        var first = 1.5f;
        var interval = 1.0f + cast.getRadiating()/4.0f;

        var delayFirst = 5;
        var delayInterval = Math.max(0, 3 - cast.getTiming());

        var yaw = Math.toRadians(player.getYHeadRot() + 90);

        double minY = player.getY() - 5;
        double maxY = player.getY() + 1.0D;

        boolean createdAny = false;
        for(int i=0;i<(3 + cast.getPower());i++)
        {
            float distance = first + interval * i;
            var pX = player.getX() + Math.cos(yaw) * distance;
            var pZ = player.getZ() + Math.sin(yaw) * distance;

            createdAny |= createSpellEntity(player, pX, pZ, minY, maxY, (float)yaw, delayFirst + delayInterval * i);
        }
        return createdAny ? spellcast : null;
    }

    @Override
    public void onImpact(InitializedSpellcast cast, HitResult mop)
    {
        SpellEffect effect = cast.getEffect();

        if (mop.getType() == HitResult.Type.ENTITY)
        {
            effect.processDirectHit(cast, ((EntityHitResult) mop).getEntity(), mop.getLocation());
        }

        effect.spawnBallParticles(cast, mop);

        if (!effect.processEntitiesAroundBefore(cast, mop.getLocation()))
            return;

        int force = cast.getDamageForce();
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
                        BlockHitResult mop2 = cast.world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cast.player));
                        if (mop2.getType() != HitResult.Type.MISS)
                            if (!mop2.getBlockPos().equals(np))
                                continue;

                        float r = (float) Math.sqrt(r2);


                        BlockState currentState = cast.world.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, mop.getLocation());
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }
}
