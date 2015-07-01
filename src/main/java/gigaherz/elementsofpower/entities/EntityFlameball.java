package gigaherz.elementsofpower.entities;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSnow;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;

public class EntityFlameball extends EntityThrowable implements IVariableSize {

    public int damageForce;

    public EntityFlameball(World worldIn)
    {
        super(worldIn);
    }

    public EntityFlameball(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
    }

    public EntityFlameball(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public EntityFlameball(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
        damageForce = force;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.0F;
    }

    private float getRandomForParticle()
    {
        return (rand.nextFloat()-0.5f) * damageForce / 8;
    }

    @Override
    protected void onImpact(MovingObjectPosition p_70184_1_)
    {
        if (p_70184_1_.entityHit != null)
        {
            int b0 = damageForce;

            if (p_70184_1_.entityHit instanceof EntityBlaze)
            {
                b0 = 3 + damageForce;
            }

            p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) b0);
        }

        for (int i = 0; i < 80; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY, this.posZ,
                    getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
        }

        if(p_70184_1_.entityHit == null && !worldObj.isRemote && damageForce > 0) {
            BlockPos bp = p_70184_1_.getBlockPos();

            if(p_70184_1_.sideHit == EnumFacing.UP) bp=bp.up();
            else if(p_70184_1_.sideHit == EnumFacing.DOWN) bp=bp.down();
            else if(p_70184_1_.sideHit == EnumFacing.EAST) bp=bp.east();
            else if(p_70184_1_.sideHit == EnumFacing.WEST) bp=bp.west();
            else if(p_70184_1_.sideHit == EnumFacing.NORTH) bp=bp.north();
            else if(p_70184_1_.sideHit == EnumFacing.SOUTH) bp=bp.south();

            int px = bp.getX();
            int py = bp.getY();
            int pz = bp.getZ();
            for (int z = pz - damageForce; z <= pz + damageForce; z++) {
                for (int x = px - damageForce; x <= px + damageForce; x++) {
                    for (int y = py - damageForce; y <= py + damageForce; y++) {
                        float dx = Math.abs(px - x);
                        float dy = Math.abs(py - y);
                        float dz = Math.abs(pz - z);
                        float r2 = (dx * dx + dy * dy + dz * dz);
                        boolean in_sphere = r2 <= (damageForce * damageForce);
                        if (!in_sphere)
                            continue;

                        float r = (float) Math.sqrt(r2);

                        BlockPos np = new BlockPos(x, y, z);


                        IBlockState currentState = worldObj.getBlockState(np);
                        Block block = currentState.getBlock();

                        if (block == Blocks.air) {
                            worldObj.setBlockState(np, Blocks.fire.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }

    @Override
    public float getScale() {
        return 0.25f * (1+damageForce);
    }
}
