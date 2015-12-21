package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ISpellEffect
{
    ISpellcast castSpell(ItemStack stack, EntityPlayer player);

    MagicAmounts getSpellCost();

    boolean isBeam();

    float getDuration();

    String getSequence();

    ISpellcast getNewCast();
}
