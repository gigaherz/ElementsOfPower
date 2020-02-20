package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.database.EssenceConversions;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MagicSourceSlot extends SlotItemHandler
{
    private final EssenceConversions magicDatabase;

    public MagicSourceSlot(EssenceConversions magicDatabase, IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
        this.magicDatabase = magicDatabase;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || magicDatabase.itemHasEssence(stack.getItem());
    }

    @Override
    public int getSlotStackLimit()
    {
        return 64;
    }
}
