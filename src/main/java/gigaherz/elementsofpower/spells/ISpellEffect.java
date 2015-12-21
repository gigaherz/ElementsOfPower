package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ISpellEffect
{
    ISpellcast castSpell(ItemStack stack, EntityPlayer player);

    MagicAmounts getSpellCost();

    int getColor();

    boolean isBeam();

    int getDuration();

    String getSequence();

    ISpellcast getNewCast();

    ISpellEffect withColor(int color);

    float getScale();
}
