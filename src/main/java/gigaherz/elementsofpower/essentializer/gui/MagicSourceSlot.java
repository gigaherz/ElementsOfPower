package gigaherz.elementsofpower.essentializer.gui;

import gigaherz.elementsofpower.database.IConversionCache;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MagicSourceSlot extends SlotItemHandler
{
    private final IConversionCache magicDatabase;

    public MagicSourceSlot(IConversionCache magicDatabase, IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
        this.magicDatabase = magicDatabase;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || magicDatabase.hasEssences(stack);
    }

    @Override
    public int getSlotStackLimit()
    {
        return 64;
    }
}
