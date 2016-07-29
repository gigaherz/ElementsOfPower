package gigaherz.elementsofpower.spelldust;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.ItemRegistered;
import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemSpelldust extends ItemRegistered
{
    public ItemSpelldust(String name)
    {
        super(name);
        setCreativeTab(ElementsOfPower.tabMagic);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (Gemstone g : Gemstone.values)
        {
            if (g != Gemstone.Creativite)
                subItems.add(new ItemStack(this, 1, g.ordinal()));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= Gemstone.values.length)
            return getUnlocalizedName();

        return getUnlocalizedName() + Gemstone.values[sub].getUnlocalizedName();
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
        BlockPos blockpos = flag ? pos : pos.offset(facing);

        if (playerIn.canPlayerEdit(blockpos, facing, stack) &&
                worldIn.canBlockBePlaced(worldIn.getBlockState(blockpos).getBlock(), blockpos, false, facing, null, stack) &&
                ElementsOfPower.spell_wire.canPlaceBlockAt(worldIn, blockpos))
        {
            --stack.stackSize;
            worldIn.setBlockState(blockpos, ElementsOfPower.spell_wire.onBlockPlaced(worldIn, blockpos, facing, hitX, hitY, hitZ, stack.getMetadata(), playerIn));
            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }
}
