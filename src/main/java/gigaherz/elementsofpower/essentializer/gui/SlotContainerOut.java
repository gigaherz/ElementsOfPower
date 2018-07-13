package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotContainerOut extends SlotItemHandler
{
    public SlotContainerOut(IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack.getCount() <= 0 || CapabilityMagicContainer.hasContainer(stack);
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
