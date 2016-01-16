package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.spells.cast.effects.SpellEffect;
import gigaherz.elementsofpower.spells.cast.shapes.SpellcastBeam;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SpellBeam extends Spell<SpellBeam, SpellcastBeam>
{
    public SpellBeam(SpellEffect effect)
    {
        super(effect);
    }

    @Override
    public SpellcastBeam getNewCast()
    {
        return new SpellcastBeam(this, effect);
    }

    @Override
    public SpellcastBeam castSpell(ItemStack stack, EntityPlayer player)
    {
        return getNewCast();
    }
}
