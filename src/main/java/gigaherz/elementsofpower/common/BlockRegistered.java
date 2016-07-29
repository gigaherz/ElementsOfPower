package gigaherz.elementsofpower.common;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

public class BlockRegistered extends Block
{
    public BlockRegistered(String name, Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
        setRegistryName(name);
        setUnlocalizedName(ElementsOfPower.MODID + "." + name);
    }

    public BlockRegistered(String name, Material materialIn)
    {
        super(materialIn);
        setRegistryName(name);
        setUnlocalizedName(ElementsOfPower.MODID + "." + name);
    }

    public ItemBlock createItemBlock()
    {
        return (ItemBlock) new ItemBlock(this).setRegistryName(getRegistryName());
    }
}
