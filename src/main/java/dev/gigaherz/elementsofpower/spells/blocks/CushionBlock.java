package dev.gigaherz.elementsofpower.spells.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CushionBlock extends DustBlock
{
    public CushionBlock(Properties properties)
    {
        super(properties);
    }

    @Deprecated
    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn)
    {
        double maxV = 0.1;
        double maxVSq = maxV * maxV;
        double factor = 0.2;

        double velocitySq = entityIn.getDeltaMovement().lengthSqr();
        if (velocitySq > maxVSq)
        {
            double velocity = Math.sqrt(velocitySq);
            double newVel = velocity + factor * (maxV - velocity);

            double ratio = newVel / velocity;

            entityIn.setDeltaMovement(entityIn.getDeltaMovement().scale(ratio));

            entityIn.fallDistance = (float) (entityIn.fallDistance * ratio);
        }
    }
}
