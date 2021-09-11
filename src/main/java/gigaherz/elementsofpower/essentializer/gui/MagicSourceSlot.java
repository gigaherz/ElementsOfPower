package gigaherz.elementsofpower.essentializer.gui;

import com.ldtteam.aequivaleo.api.results.IEquivalencyResults;
import gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MagicSourceSlot extends SlotItemHandler
{
    private final IEquivalencyResults magicDatabase;

    public MagicSourceSlot(IEquivalencyResults magicDatabase, IItemHandler inventory, int par2, int par3, int par4)
    {
        super(inventory, par2, par3, par4);
        this.magicDatabase = magicDatabase;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || AequivaleoPlugin.getEssences(magicDatabase, stack, false).isPresent();
    }

    @Override
    public int getSlotStackLimit()
    {
        return 64;
    }
}
