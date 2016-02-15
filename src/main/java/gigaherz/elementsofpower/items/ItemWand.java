package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;

public class ItemWand extends ItemGemContainer
{
    public ItemWand()
    {
        super();
        setUnlocalizedName(ElementsOfPower.MODID + ".wand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }
}