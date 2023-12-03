package dev.gigaherz.elementsofpower.essentializer.menu;

import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MagicContainerOutputSlot extends SlotItemHandler
{
    public MagicContainerOutputSlot(IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return stack.getCount() <= 0 || MagicContainerCapability.hasContainer(stack);
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }
}
