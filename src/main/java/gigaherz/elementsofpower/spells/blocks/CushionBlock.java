package gigaherz.elementsofpower.spells.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CushionBlock extends DustBlock
{
    public CushionBlock(Properties properties)
    {
        super(properties);
    }

    @Deprecated
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
    {
        double maxV = 0.1;
        double maxVSq = maxV * maxV;
        double factor = 0.2;

        double velocitySq = entityIn.getMotion().lengthSquared();
        if (velocitySq > maxVSq)
        {
            double velocity = Math.sqrt(velocitySq);
            double newVel = velocity + factor * (maxV - velocity);

            double ratio = newVel / velocity;

            entityIn.setMotion(entityIn.getMotion().scale(ratio));

            entityIn.fallDistance = (float) (entityIn.fallDistance * ratio);
        }
    }
}
