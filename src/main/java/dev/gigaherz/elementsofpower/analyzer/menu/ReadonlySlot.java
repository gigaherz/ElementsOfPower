package dev.gigaherz.elementsofpower.analyzer.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ReadonlySlot extends Slot
{
    public ReadonlySlot(Container par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean mayPlace(ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn)
    {
        return false;
    }
}
