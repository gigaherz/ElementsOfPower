package gigaherz.elementsofpower.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSnow;
import net.minecraft.util.*;
import net.minecraft.world.World;

public abstract class EntityBallBase extends EntityThrowable implements IVariableSize {

    public int damageForce;

    public EntityBallBase(World worldIn)
    {
        super(worldIn);
    }

    public EntityBallBase(World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
    }

    public EntityBallBase(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public EntityBallBase(World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
        damageForce = force;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.0F;
    }

    protected float getRandomForParticle()
    {
        return (rand.nextFloat()-0.5f) * damageForce / 8;
    }

    @Override
    protected void onImpact(MovingObjectPosition pos)
    {
        if (pos.entityHit != null)
        {
            processDirectHit(pos.entityHit);
        }

        spawnBallParticles();

        if(!worldObj.isRemote && damageForce > 0) {
            BlockPos bp = pos.getBlockPos();

            if(bp != null) {
                if (pos.sideHit == EnumFacing.UP) bp = bp.up();
                else if (pos.sideHit == EnumFacing.DOWN) bp = bp.down();
                else if (pos.sideHit == EnumFacing.EAST) bp = bp.east();
                else if (pos.sideHit == EnumFacing.WEST) bp = bp.west();
                else if (pos.sideHit == EnumFacing.NORTH) bp = bp.north();
                else if (pos.sideHit == EnumFacing.SOUTH) bp = bp.south();
            }
            else
            {
                bp = new BlockPos(
                        pos.hitVec.xCoord,
                        pos.hitVec.yCoord,
                        pos.hitVec.zCoord);
            }

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

                        int layers = (int)Math.min(damageForce - r, 7);

                        BlockPos np = new BlockPos(x, y, z);

                        IBlockState currentState = worldObj.getBlockState(np);

                        processBlockWithinRadius(np, currentState, layers);
                    }
                }
            }
        }

        processEntitiesAround(pos.hitVec);

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }

    protected void processDirectHit(Entity entityHit) {}

    protected void processEntitiesAround(Vec3 hitVec) {}

    protected abstract void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers);

    protected abstract void spawnBallParticles();

    @Override
    public float getScale() {
        return 0.25f * (1+damageForce);
    }
}
