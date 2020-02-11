package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class SingleShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast)
    {
        return cast;
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        RayTraceResult mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.getType() == RayTraceResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, ((EntityRayTraceResult)mop).getEntity(), mop.getHitVec());
            }
            else if (mop.getType() == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = ((BlockRayTraceResult)mop).getPos();
                BlockState state = cast.world.getBlockState(pos);
                cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }
        }
    }
}
