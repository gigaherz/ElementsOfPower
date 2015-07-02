package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ISpellEffect {
    void castSpell(ItemStack stack, EntityPlayer player);

    MagicAmounts getSpellCost();
}
