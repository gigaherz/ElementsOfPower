package gigaherz.elementsofpower.analyzer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gui.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAnalyzer extends Item
{
    public ItemAnalyzer()
    {
        setMaxStackSize(1);
        setUnlocalizedName(ElementsOfPower.MODID + ".analyzer");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        pos = playerIn.getPosition();
        if (!worldIn.isRemote)
            playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ANALYZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        BlockPos pos = playerIn.getPosition();
        if (!worldIn.isRemote)
            playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ANALYZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
    }
}
