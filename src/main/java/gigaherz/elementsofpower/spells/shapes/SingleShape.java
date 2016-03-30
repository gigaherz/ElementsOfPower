package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class SingleShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
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
            if (mop.typeOfHit == RayTraceResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, mop.entityHit, mop.hitVec);
            }
            else if (mop.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = mop.getBlockPos();
                IBlockState state = cast.world.getBlockState(pos);
                cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }
        }
    }
}
