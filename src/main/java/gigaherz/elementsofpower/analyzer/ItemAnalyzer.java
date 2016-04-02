package gigaherz.elementsofpower.analyzer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gui.GuiHandler;
import gigaherz.elementsofpower.items.ItemRegistered;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        pos = playerIn.getPosition();
        if (!worldIn.isRemote)
            playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ANALYZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        BlockPos pos = playerIn.getPosition();
        if (!worldIn.isRemote)
            playerIn.openGui(ElementsOfPower.instance, GuiHandler.GUI_ANALYZER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return super.onItemRightClick(itemStackIn, worldIn, playerIn);
    }
}
