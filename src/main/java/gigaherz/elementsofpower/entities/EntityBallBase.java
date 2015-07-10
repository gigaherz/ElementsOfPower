package gigaherz.elementsofpower.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityBallBase extends EntityThrowable implements IVariableSize, IRenderStackProvider {

    public int damageForce;
    private ItemStack stackForRendering;

    public EntityBallBase(ItemStack stack, World worldIn)
    {
        super(worldIn);
        stackForRendering = stack;
    }

    public EntityBallBase(ItemStack stack, World worldIn, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
        stackForRendering = stack;
    }

    public EntityBallBase(ItemStack stack, World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
        stackForRendering = stack;
    }

    public EntityBallBase(ItemStack stack, World worldIn, int force, EntityLivingBase p_i1774_2_)
    {
        super(worldIn, p_i1774_2_);
        this.addVelocity(0, p_i1774_2_.motionY, 0);
        damageForce = force;
        stackForRendering = stack;
    }

    public ItemStack getStackForRendering()
    {
        return stackForRendering;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

    protected float getRandomForParticle()
    {
        return (rand.nextFloat()-0.5f) * damageForce / 8;
    }

    @Override
    protected void onImpact(MovingObjectPosition pos)
    {
        int force = getDamageForce();

        if (pos.entityHit != null)
        {
            processDirectHit(pos.entityHit);
        }

        spawnBallParticles();

        processEntitiesAroundBefore(pos.hitVec);

        if(!worldObj.isRemote && force > 0) {
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
                bp = new BlockPos(pos.hitVec);
            }

            int px = bp.getX();
            int py = bp.getY();
            int pz = bp.getZ();
            for (int z = pz - force; z <= pz + force; z++) {
                for (int x = px - force; x <= px + force; x++) {
                    for (int y = py - force; y <= py + force; y++) {
                        float dx = Math.abs(px - x);
                        float dy = Math.abs(py - y);
                        float dz = Math.abs(pz - z);
                        float r2 = (dx * dx + dy * dy + dz * dz);
                        boolean in_sphere = r2 <= (force * force);
                        if (!in_sphere)
                            continue;

                        float r = (float) Math.sqrt(r2);

                        int layers = (int)Math.min(force - r, 7);

                        BlockPos np = new BlockPos(x, y, z);

                        IBlockState currentState = worldObj.getBlockState(np);

                        processBlockWithinRadius(np, currentState, layers);
                    }
                }
            }
        }

        processEntitiesAroundAfter(pos.hitVec);

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }

    protected void processDirectHit(Entity entityHit) {}

    protected void processEntitiesAroundBefore(Vec3 hitVec) {}
    protected void processEntitiesAroundAfter(Vec3 hitVec) {}

    protected abstract void processBlockWithinRadius(BlockPos blockPos, IBlockState currentState, int layers);

    protected abstract void spawnBallParticles();

    @Override
    public float getScale() {
        return 0.25f * (1+damageForce);
    }

    public int getDamageForce() {
        return damageForce;
    }
}
