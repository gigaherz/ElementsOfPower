package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public abstract class SpellShape
{
    public void spellTick(Spellcast spellcast)
    {
    }

    public abstract Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast);

    public void onImpact(Spellcast spellcast, MovingObjectPosition mop)
    {
    }

    public float getScale(Spellcast spellcast)
    {
        return 1;
    }

    public abstract boolean isInstant();
}
