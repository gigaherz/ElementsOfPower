package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

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
        MovingObjectPosition mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, mop.entityHit, mop.hitVec);
            }
            else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos pos = mop.getBlockPos();
                IBlockState state = cast.world.getBlockState(pos);
                cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }
        }
    }
}
