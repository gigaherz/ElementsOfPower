package gigaherz.elementsofpower.items;

import baubles.api.BaubleType;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

public class ItemNecklace extends ItemBauble
{
    public ItemNecklace(String name)
    {
        super(name);
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(ItemStack itemstack)
    {
        return BaubleType.AMULET;
    }
}
