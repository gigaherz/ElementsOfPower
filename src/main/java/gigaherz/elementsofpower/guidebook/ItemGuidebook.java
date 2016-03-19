package gigaherz.elementsofpower.guidebook;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemGuidebook extends Item
{
    public ItemGuidebook()
    {
        setMaxStackSize(1);
        setUnlocalizedName(ElementsOfPower.MODID + ".guidebook");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiGuidebook());
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        if (worldIn.isRemote)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiGuidebook());
        }
        return super.onItemRightClick(itemStackIn, worldIn, playerIn);
    }
}
