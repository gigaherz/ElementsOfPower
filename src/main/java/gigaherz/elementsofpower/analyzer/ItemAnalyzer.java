package gigaherz.elementsofpower.analyzer;

import gigaherz.common.ItemRegistered;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAnalyzer extends ItemRegistered
{
    public ItemAnalyzer(String name)
    {
        super(name);
        setMaxStackSize(1);
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        pos = playerIn.getPosition();
        if (!worldIn.isRemote)
            playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ANALYZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        BlockPos pos = playerIn.getPosition();
        if (!worldIn.isRemote)
            playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ANALYZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }
}
