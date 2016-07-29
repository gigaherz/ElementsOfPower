package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class WitherEffect extends SpellEffect
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

    private void witherEntities(Spellcast cast, Vec3d hitVec, List<? extends EntityLivingBase> living)
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

        causePotionEffect(cast, e, MobEffects.WITHER, 0, lv, 100.0);
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
        if (entity instanceof EntityLivingBase)
            applyEffectsToEntity(cast, 0, (EntityLivingBase) entity);
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
        witherEntities(cast, hitVec, living);
    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        cast.spawnRandomParticle(EnumParticleTypes.FLAME,
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.GRASS)
        {
            cast.world.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
        }
        else if (block == Blocks.DIRT)
        {
            switch (currentState.getValue(BlockDirt.VARIANT))
            {
                case DIRT:
                    cast.world.setBlockState(blockPos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT));
                    break;
            }
        }
    }
}
