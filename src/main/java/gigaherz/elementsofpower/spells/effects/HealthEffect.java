package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

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

    private void healEntities(Spellcast cast, Vec3d hitVec, List<? extends EntityLivingBase> living)
    {
        for (EntityLivingBase e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            applyEffectsToEntity(cast, distance, e);
        }
    }

    private void applyEffectsToEntity(Spellcast cast, double distance, EntityLivingBase e)
    {
        double lv = Math.max(0, cast.getDamageForce() - distance);

        int emp = cast.getEmpowering();

        if (-emp < lv)
            causePotionEffect(cast, e, MobEffects.heal, 0, (lv + emp) * 0.5, 0.0);

        if (emp < lv)
            causePotionEffect(cast, e, MobEffects.regeneration, 0, (lv - emp), 100.0);
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {
        if (e instanceof EntityLivingBase)
            applyEffectsToEntity(cast, 0, (EntityLivingBase) e);
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
                hitVec.xCoord - cast.getDamageForce(),
                hitVec.yCoord - cast.getDamageForce(),
                hitVec.zCoord - cast.getDamageForce(),
                hitVec.xCoord + cast.getDamageForce(),
                hitVec.yCoord + cast.getDamageForce(),
                hitVec.zCoord + cast.getDamageForce());

        List<EntityLivingBase> living = cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        healEntities(cast, hitVec, living);
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        cast.spawnRandomParticle(EnumParticleTypes.FLAME,
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, RayTraceResult mop)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.dirt)
        {
            switch (currentState.getValue(BlockDirt.VARIANT))
            {
                case COARSE_DIRT:
                    cast.world.setBlockState(blockPos, Blocks.dirt.getDefaultState());
                    break;
                case DIRT:
                    cast.world.setBlockState(blockPos, Blocks.grass.getDefaultState());
                    break;
            }
        }
    }
}
