package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;

public class WitherEffect extends SpellEffect
{
    @Override
    public int getColor(InitializedSpellcast cast)
    {
        return 0xA0E0FF;
    }

    @Override
    public int getDuration(InitializedSpellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(InitializedSpellcast cast)
    {
        return 8;
    }

    private void witherEntities(InitializedSpellcast cast, Vector3d hitVec, List<? extends LivingEntity> living)
    {
        for (LivingEntity e : living)
        {
            if (!e.isAlive())
                continue;

            double dx = e.getPosX() - hitVec.x;
            double dy = e.getPosY() - hitVec.y;
            double dz = e.getPosZ() - hitVec.z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffectsToEntity(cast, distance, e);
        }
    }

    private void applyEffectsToEntity(InitializedSpellcast cast, double distance, LivingEntity e)
    {
        double lv = Math.max(0, cast.getDamageForce() - distance);

        causePotionEffect(cast, e, Effects.WITHER, 0, lv, 100.0);
    }

    @Override
    public void processDirectHit(InitializedSpellcast cast, Entity entity, Vector3d hitVec)
    {
        if (entity instanceof LivingEntity)
            applyEffectsToEntity(cast, 0, (LivingEntity) entity);
    }

    @Override
    public boolean processEntitiesAroundBefore(InitializedSpellcast cast, Vector3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(InitializedSpellcast cast, Vector3d hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.x - cast.getDamageForce(),
                hitVec.y - cast.getDamageForce(),
                hitVec.z - cast.getDamageForce(),
                hitVec.x + cast.getDamageForce(),
                hitVec.y + cast.getDamageForce(),
                hitVec.z + cast.getDamageForce());

        List<LivingEntity> living = cast.world.getEntitiesWithinAABB(LivingEntity.class, aabb);
        witherEntities(cast, hitVec, living);
    }

    @Override
    public void spawnBallParticles(InitializedSpellcast cast, RayTraceResult mop)
    {
        Vector3d hitVec = mop.getHitVec();
        cast.spawnRandomParticle(ParticleTypes.FLAME, hitVec.x, hitVec.y, hitVec.z);
    }

    @Override
    public void processBlockWithinRadius(InitializedSpellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.GRASS_BLOCK)
        {
            cast.world.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
        }
        else if (block == Blocks.DIRT)
        {
            cast.world.setBlockState(blockPos, Blocks.COARSE_DIRT.getDefaultState());
        }
    }
}
