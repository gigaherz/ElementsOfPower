package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.item.ItemStack;

public class ItemStaff extends ItemWand
{
    public ItemStaff()
    {
        super();
        setUnlocalizedName(ElementsOfPower.MODID + ".staff");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        MagicAmounts magic = super.getCapacity(stack);
        if (magic == null)
            return null;

        magic.add(magic.copy());
        return magic;
    }

    @Override
    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        if (am == null)
            return null;

        return am.copy().multiply(2.0f);
    }

    @Override
    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        if (am == null)
            return null;

        return am.copy().multiply(0.5f);
    }
}
