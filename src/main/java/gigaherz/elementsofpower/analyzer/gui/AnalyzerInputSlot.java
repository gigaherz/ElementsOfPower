package gigaherz.elementsofpower.analyzer.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class AnalyzerInputSlot extends Slot
{
    public AnalyzerInputSlot(IInventory inventory, int slotNumber, int x, int y)
    {
        super(inventory, slotNumber, x, y);
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return 1;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
