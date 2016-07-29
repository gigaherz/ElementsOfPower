package gigaherz.elementsofpower.spells.blocks;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCushion extends BlockDust
{
    public BlockCushion(String name)
    {
        super(name, ElementsOfPower.materialCushion);
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }

    @Deprecated
    @Override
    public int getLightOpacity(IBlockState state)
    {
        return 0;
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        double maxV = 0.1;
        double maxVSq = maxV * maxV;
        double factor = 0.2;

        double velocitySq = entityIn.motionX * entityIn.motionX + entityIn.motionY * entityIn.motionY + entityIn.motionZ * entityIn.motionZ;
        if (velocitySq > maxVSq)
        {
            double velocity = Math.sqrt(velocitySq);
            double newVel = velocity + factor * (maxV - velocity);

            entityIn.motionX = entityIn.motionX * newVel / velocity;
            entityIn.motionY = entityIn.motionY * newVel / velocity;
            entityIn.motionZ = entityIn.motionZ * newVel / velocity;

            entityIn.fallDistance = (float) (entityIn.fallDistance * newVel / velocity);
        }
    }
}
