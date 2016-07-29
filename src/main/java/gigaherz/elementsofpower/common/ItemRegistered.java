package gigaherz.elementsofpower.common;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.Item;

public class ItemRegistered extends Item
{
    public ItemRegistered(String name)
    {
        setRegistryName(name);
        setUnlocalizedName(ElementsOfPower.MODID + "." + name);
    }
}
