package gigaherz.elementsofpower.gemstones;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.BlockRegistered;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class BlockGemstone extends BlockRegistered
{
    public static final PropertyEnum<GemstoneBlockType> TYPE = PropertyEnum.create("type", GemstoneBlockType.class);

    public BlockGemstone(String name)
    {
        super(name, Material.IRON, MapColor.DIAMOND);
        setHardness(5.0F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).ordinal();
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if (meta > GemstoneBlockType.values.length)
            return getDefaultState();
        return getDefaultState().withProperty(TYPE, GemstoneBlockType.values[meta]);
    }

    public ItemStack getStack(GemstoneBlockType gemstoneBlockType)
    {
        return getStack(1, gemstoneBlockType);
    }

    public ItemStack getStack(int quantity, GemstoneBlockType gemstoneBlockType)
    {
        return new ItemStack(this, quantity, getMetaFromState(getDefaultState().withProperty(TYPE, gemstoneBlockType)));
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (GemstoneBlockType type : GemstoneBlockType.values)
        {
            list.add(getStack(1, type));
        }
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return (ItemBlock) new ItemForm(this).setRegistryName(getRegistryName());
    }

    public static class ItemForm extends ItemBlock
    {
        public ItemForm(Block block)
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
            if (stack.getMetadata() > GemstoneBlockType.values.length)
                return block.getUnlocalizedName();
            return "tile." + ElementsOfPower.MODID + "." + GemstoneBlockType.values[stack.getMetadata()] + "Block";
        }
    }
}
