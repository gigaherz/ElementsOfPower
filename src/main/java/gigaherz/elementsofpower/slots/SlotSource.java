package gigaherz.elementsofpower.slots;

import gigaherz.elementsofpower.MagicDatabase;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotSource extends Slot {
    public SlotSource(IInventory par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return MagicDatabase.itemContainsMagic(par1ItemStack)
                || MagicDatabase.itemHasEssence(par1ItemStack);
    }

    @Override
    public int getSlotStackLimit() {
        return 64;
    }
}
