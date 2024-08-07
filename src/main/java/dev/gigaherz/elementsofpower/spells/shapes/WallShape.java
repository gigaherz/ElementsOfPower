package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.entities.PillarEntity;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;


public class WallShape extends SpellShape
{
    @Override
    public float getScale(SpellcastState cast)
    {
        return 1 + 0.25f * cast.damageForce();
    }

    private boolean createSpellEntity(Player player, Spellcast cast, double posX, double posZ, double minY, double maxY, float yaw, int delayTicks) {
        BlockPos blockpos = BlockPos.containing(posX, maxY, posZ);
        boolean flag = false;
        double d0 = 0.0D;

        do
        {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = player.level().getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(player.level(), blockpos1, Direction.UP)) {
                if (!player.level().isEmptyBlock(blockpos)) {
                    BlockState blockstate1 = player.level().getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(player.level(), blockpos);
                    if (!voxelshape.isEmpty()) {
                        d0 = voxelshape.max(Direction.Axis.Y);
                    }
                }

                flag = true;
                break;
            }

            blockpos = blockpos.below();
        }
        while(blockpos.getY() >= Mth.floor(minY) - 1);

        double posY = blockpos.getY() + d0;

        if (flag)
        {
            return player.level().addFreshEntity(new PillarEntity(player.level(), player, cast, posX, posY, posZ, yaw, delayTicks));
        }

        return false;
    }

    @Override
    public Spellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        var first = 1.5f;
        var interval = 1.0f + cast.radiating() /4.0f;

        var delayFirst = 5;
        var delayInterval = 3; // Math.max(0, 3 - cast.getTiming());

        var yaw = player.getYRot();

        double minY = player.getY() - 5;
        double maxY = player.getY() + 1.0D;

        boolean createdAny = false;
        for(int i = 0; i<(3 + cast.power()); i++)
        {
            float distance = first + interval * i;
            var pX = player.getX() + Math.cos(Math.toRadians(yaw + 90)) * distance;
            var pZ = player.getZ() + Math.sin(Math.toRadians(yaw + 90)) * distance;

            createdAny |= createSpellEntity(player, cast, pX, pZ, minY, maxY, yaw, delayFirst + delayInterval * i);
        }
        return createdAny ? cast : null;
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
