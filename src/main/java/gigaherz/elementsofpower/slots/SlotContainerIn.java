package gigaherz.elementsofpower.slots;

import gigaherz.elementsofpower.database.ContainerInformation;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotContainerIn extends Slot
{
    public SlotContainerIn(IInventory par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack)
    {
        return ContainerInformation.itemContainsMagic(par1ItemStack);
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
