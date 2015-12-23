package gigaherz.elementsofpower.spells.cast.beams;

import gigaherz.elementsofpower.spells.SpellBeam;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class Firebeam extends BeamBase
{
    public Firebeam(SpellBeam spell)
    {
        super(spell);
    }

    @Override
    protected void applyEffect(MovingObjectPosition mop)
    {
        if (mop == null)
            return;

        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            BlockPos pos = mop.getBlockPos().offset(mop.sideHit);
            if (world.getBlockState(pos).getBlock() == Blocks.air)
            {
                world.setBlockState(pos, Blocks.fire.getDefaultState());
            }
        }
        else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
        {
            // TODO: this.applyEnchantments(this.getCaster(), hitInfo.entityHit);

            if (!mop.entityHit.isImmuneToFire())
            {
                mop.entityHit.setFire(spell.getPower());
            }
        }
    }
}
