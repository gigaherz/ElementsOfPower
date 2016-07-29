package gigaherz.elementsofpower.guidebook;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.ItemRegistered;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemGuidebook extends ItemRegistered
{
    public ItemGuidebook(String name)
    {
        super(name);
        setMaxStackSize(1);
        setUnlocalizedName(ElementsOfPower.MODID + ".guidebook");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            ElementsOfPower.proxy.displayBook();
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            ElementsOfPower.proxy.displayBook();
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
    }
}
