package gigaherz.elementsofpower.blocks;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.GemstoneBlockType;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class BlockGemstone extends Block
{
    public static final PropertyEnum<GemstoneBlockType> TYPE = PropertyEnum.create("type", GemstoneBlockType.class);

    public BlockGemstone()
    {
        super(Material.iron, MapColor.diamondColor);

        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabBlock);
        setUnlocalizedName(ElementsOfPower.MODID + ".gemstoneBlock");
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(TYPE, GemstoneBlockType.values[meta]);
    }

    public ItemStack getStack(int quantity, GemstoneBlockType gemstoneBlockType)
    {
        return new ItemStack(Item.getItemFromBlock(this), quantity, getMetaFromState(getDefaultState().withProperty(TYPE, gemstoneBlockType)));
    }

    @Override
    public void getSubBlocks(net.minecraft.item.Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for(GemstoneBlockType type : GemstoneBlockType.values)
        {
            list.add(getStack(1, type));
        }
    }

    public static class Item extends ItemBlock
    {
        public Item(Block block)
        {
            super(block);
            setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage)
        {
            return damage;
        }

        @Override
        public String getUnlocalizedName(ItemStack stack)
        {
            if(stack.getMetadata() > GemstoneBlockType.values.length)
                return block.getUnlocalizedName();
            return "tile." + ElementsOfPower.MODID + "." + GemstoneBlockType.values[stack.getMetadata()] + "Block";
        }
    }
}
