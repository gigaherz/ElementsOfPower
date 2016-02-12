package gigaherz.elementsofpower.slots;

import gigaherz.elementsofpower.database.ContainerInformation;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotAnalyzerIn extends Slot
{
    boolean isRemote;

    public SlotAnalyzerIn(IInventory par1iInventory, int par2, int par3, int par4, boolean isRemote)
    {
        super(par1iInventory, par2, par3, par4);
        this.isRemote = isRemote;
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

    @Override
    public void putStack(ItemStack stack)
    {
        if(stack != null && !isRemote)
            stack = ContainerInformation.identifyQuality(stack);
        super.putStack(stack);
    }
}
