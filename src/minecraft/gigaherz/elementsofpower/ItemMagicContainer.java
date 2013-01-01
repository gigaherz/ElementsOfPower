package gigaherz.elementsofpower;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMagicContainer extends Item
{
    public ItemMagicContainer(int id)
    {
        super(id);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack)
    {
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        if (amounts == null)
        {
            return false;
        }

        return !amounts.isEmpty();
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltipList, boolean showAdvancedInfo)
    {
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        if (amounts == null)
        {
            return;
        }

        for (int i = 0; i < 8; i++)
        {
            if (amounts.amounts[i] == 0)
            {
                continue;
            }

            String magicName = MagicDatabase.getMagicName(i);
            String str = String.format("%s x%d", magicName, amounts.amounts[i]);
            tooltipList.add(str);
        }
    }
}
