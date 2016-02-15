package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SelfShape extends SpellShape
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
        cast.getEffect().processDirectHit(cast, cast.player);
    }
}
