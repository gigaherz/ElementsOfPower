package gigaherz.elementsofpower.items;

import baubles.api.BaubleType;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import net.minecraft.item.ItemStack;

public class ItemNecklace extends ItemBauble
{
    public ItemNecklace(String name)
    {
        super(name);
        setCreativeTab(ElementsOfPowerMod.tabMagic);
    }

    @Override
    protected Object getBaubleInstance()
    {
        return new BaubleData()
        {
            @Override
            public BaubleType getBaubleType(ItemStack itemstack)
            {
                return BaubleType.AMULET;
            }
        };
    }
}
