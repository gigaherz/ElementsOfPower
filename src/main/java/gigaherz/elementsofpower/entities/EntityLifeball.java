package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class EntityLifeball extends EntityBallBase
{

    public EntityLifeball(World worldIn)
    {
        super(ElementsOfPower.air, worldIn);
    }

    public EntityLifeball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.air, worldIn, p_i1774_2_);
    }

    public EntityLifeball(World worldIn, double x, double y, double z)
    {
        super(ElementsOfPower.air, worldIn, x, y, z);
    }

    public EntityLifeball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.air, worldIn, force, p_i1774_2_);
    }

    @Override
    protected void processDirectHit(Entity e)
    {
        //int b0 = damageForce;
        //
        //if (e instanceof EntityBlaze)
        //{
        //    b0 = 3 + damageForce;
        //}
        //
        //e.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) b0);
    }

    @Override
    protected void processEntitiesAroundAfter(Vec3 hitVec)
    {

        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - damageForce,
                hitVec.yCoord - damageForce,
                hitVec.zCoord - damageForce,
                hitVec.xCoord + damageForce,
                hitVec.yCoord + damageForce,
                hitVec.zCoord + damageForce);

        List<EntityLivingBase> living = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
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

            double lv = Math.max(0, damageForce - ll);

            causePotionEffect(e, Potion.heal, 0, lv * 0.5, 0.0);
            causePotionEffect(e, Potion.regeneration, 0, lv, 100.0);
        }
    }

    private void causePotionEffect(EntityLivingBase e, Potion potion, int amplifier, double distance, double durationBase)
    {

        int id = potion.getId();
        if (Potion.potionTypes[id].isInstant())
        {
            Potion.potionTypes[id].affectEntity(this, this.getThrower(), e, amplifier, distance);
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
    protected void spawnBallParticles()
    {
        this.worldObj.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY, this.posZ,
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
                    worldObj.setBlockState(blockPos, Blocks.dirt.getDefaultState());
                    break;
                case DIRT:
                    worldObj.setBlockState(blockPos, Blocks.grass.getDefaultState());
                    break;
            }
        }
    }
}
