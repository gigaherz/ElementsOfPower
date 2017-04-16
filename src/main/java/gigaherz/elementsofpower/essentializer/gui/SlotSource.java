package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.database.EssenceConversions;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotSource extends SlotItemHandler
{
    public SlotSource(IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || EssenceConversions.itemHasEssence(stack);
    }

    @Override
    public int getSlotStackLimit()
    {
        return 64;
    }
}
