package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SingleShape extends SpellShape
{
    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(SpellcastState cast)
    {
        HitResult mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.getType() == HitResult.Type.ENTITY)
            {
                cast.effect().processDirectHit(cast, ((EntityHitResult) mop).getEntity(), mop.getLocation(), cast.player());
            }
            else if (mop.getType() == HitResult.Type.BLOCK)
            {
                BlockPos pos = ((BlockHitResult) mop).getBlockPos();
                BlockState state = cast.level().getBlockState(pos);
                cast.effect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }
        }
    }
}
