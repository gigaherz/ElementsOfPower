package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class EntityFlameball extends EntityBallBase {

    public EntityFlameball(World worldIn)
    {
        super(ElementsOfPower.fire, worldIn);
    }
    public EntityFlameball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.fire, worldIn, p_i1774_2_);
    }
    public EntityFlameball(World worldIn, double x, double y, double z)
    {
        super(ElementsOfPower.fire, worldIn, x, y, z);
    }
    public EntityFlameball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(ElementsOfPower.fire, worldIn, force, p_i1774_2_);
    }

    @Override
    protected void processDirectHit(Entity e)
    {
        int b0 = damageForce;

        if (e instanceof EntityBlaze)
        {
            b0 = 3 + damageForce;
        }

        e.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) b0);
    }

    @Override
    protected void processEntitiesAroundAfter(Vec3 hitVec) {

        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord-damageForce,
                hitVec.yCoord-damageForce,
                hitVec.zCoord-damageForce,
                hitVec.xCoord+damageForce,
                hitVec.yCoord+damageForce,
                hitVec.zCoord+damageForce);

        List<EntityLivingBase> living = (List<EntityLivingBase>)worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        burnEntities(hitVec, living);

        List<EntityItem> items = (List<EntityItem>)worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
        burnEntities(hitVec, items);
    }

    private void burnEntities(Vec3 hitVec, List<? extends Entity> living) {

        EntityFireball ef = new EntitySmallFireball(worldObj);

        for(Entity e : living)
        {
            if(!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx*dx+dy*dy+dz*dz);

            double lv = Math.max(0, damageForce-ll);

            boolean flag = e.attackEntityFrom(DamageSource.causeFireballDamage(ef, this.getThrower()), 5.0F);

            if (flag)
            {
                this.func_174815_a(this.getThrower(), e);

                if (!e.isImmuneToFire())
                {
                    e.setFire((int)lv);
                }
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

        if (block == Blocks.air) {
            worldObj.setBlockState(blockPos, Blocks.fire.getDefaultState());
        }
    }
}
