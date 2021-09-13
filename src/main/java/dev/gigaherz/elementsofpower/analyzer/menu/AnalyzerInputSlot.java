package dev.gigaherz.elementsofpower.analyzer.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnalyzerInputSlot extends Slot
{
    public AnalyzerInputSlot(Container inventory, int slotNumber, int x, int y)
    {
        super(inventory, slotNumber, x, y);
    }

    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        return 1;
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }
}
