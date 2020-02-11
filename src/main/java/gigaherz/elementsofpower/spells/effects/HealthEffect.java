package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class HealthEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0xA0E0FF;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 8;
    }

    private void healEntities(Spellcast cast, Vec3d hitVec, List<? extends LivingEntity> living)
    {
        for (LivingEntity e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - getHitVec().x;
            double dy = e.posY - getHitVec().y;
            double dz = e.posZ - getHitVec().z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffectsToEntity(cast, distance, e);
        }
    }

    private void applyEffectsToEntity(Spellcast cast, double distance, LivingEntity e)
    {
        double lv = Math.max(0, cast.getDamageForce() - distance);

        int emp = cast.getEmpowering();

        if (-emp < lv)
            causePotionEffect(cast, e, Effects.INSTANT_HEALTH, 0, (lv + emp) * 0.5, 0.0);

        if (emp < lv)
            causePotionEffect(cast, e, Effects.REGENERATION, 0, (lv - emp), 100.0);
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        if (entity instanceof LivingEntity)
            applyEffectsToEntity(cast, 0, (LivingEntity) entity);
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                getHitVec().x - cast.getDamageForce(),
                getHitVec().y - cast.getDamageForce(),
                getHitVec().z - cast.getDamageForce(),
                getHitVec().x + cast.getDamageForce(),
                getHitVec().y + cast.getDamageForce(),
                getHitVec().z + cast.getDamageForce());

        List<LivingEntity> living = cast.world.getEntitiesWithinAABB(LivingEntity.class, aabb);
        healEntities(cast, getHitVec(), living);
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        cast.spawnRandomParticle(EnumParticleTypes.FLAME,
                mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z);
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.DIRT)
        {
            switch (currentState.getValue(BlockDirt.VARIANT))
            {
                case COARSE_DIRT:
                    cast.world.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
                    break;
                case DIRT:
                    cast.world.setBlockState(blockPos, Blocks.GRASS.getDefaultState());
                    break;
            }
        }
    }
}
