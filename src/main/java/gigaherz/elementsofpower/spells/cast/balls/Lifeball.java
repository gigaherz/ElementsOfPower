package gigaherz.elementsofpower.spells.cast.balls;

import gigaherz.elementsofpower.spells.SpellBall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;

import java.util.List;

public class Lifeball extends BallBase
{
    public Lifeball(SpellBall parent)
    {
        super(parent);
    }

    @Override
    protected void processDirectHit(Entity e)
    {
        //int b0 = getDamageForce();
        //
        //if (e instanceof EntityBlaze)
        //{
        //    b0 = 3 + getDamageForce();
        //}
        //
        //e.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) b0);
    }

    @Override
    protected void processEntitiesAroundAfter(Vec3 hitVec)
    {

        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - getDamageForce(),
                hitVec.yCoord - getDamageForce(),
                hitVec.zCoord - getDamageForce(),
                hitVec.xCoord + getDamageForce(),
                hitVec.yCoord + getDamageForce(),
                hitVec.zCoord + getDamageForce());

        List<EntityLivingBase> living = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        burnEntities(hitVec, living);
    }

    private void burnEntities(Vec3 hitVec, List<? extends EntityLivingBase> living)
    {

        for (EntityLivingBase e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, getDamageForce() - ll);

            causePotionEffect(e, Potion.heal, 0, lv * 0.5, 0.0);
            causePotionEffect(e, Potion.regeneration, 0, lv, 100.0);
        }
    }

    private void causePotionEffect(EntityLivingBase e, Potion potion, int amplifier, double distance, double durationBase)
    {

        int id = potion.getId();
        if (Potion.potionTypes[id].isInstant())
        {
            Potion.potionTypes[id].affectEntity(projectile, player, e, amplifier, distance);
        }
        else
        {
            int j = (int) (distance * durationBase + 0.5D);

            if (j > 20)
            {
                e.addPotionEffect(new PotionEffect(id, j, amplifier));
            }
        }
    }

    @Override
    protected void spawnBallParticles(MovingObjectPosition mop)
    {
        this.world.spawnParticle(EnumParticleTypes.FLAME,
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }

    @Override
    protected void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.dirt)
        {
            switch (currentState.getValue(BlockDirt.VARIANT))
            {
                case COARSE_DIRT:
                    world.setBlockState(blockPos, Blocks.dirt.getDefaultState());
                    break;
                case DIRT:
                    world.setBlockState(blockPos, Blocks.grass.getDefaultState());
                    break;
            }
        }
    }
}
