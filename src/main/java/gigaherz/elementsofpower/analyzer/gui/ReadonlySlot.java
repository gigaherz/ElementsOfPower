package gigaherz.elementsofpower.analyzer.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ReadonlySlot extends Slot
{
    public ReadonlySlot(IInventory par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn)
    {
        return false;
    }
}
