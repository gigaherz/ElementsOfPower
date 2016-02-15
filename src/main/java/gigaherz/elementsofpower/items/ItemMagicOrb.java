package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemMagicOrb extends Item
{
    private final static String[] subNames =
            {"fire", "water", "air", "earth", "light", "darkness", "life", "death"};

    public ItemMagicOrb()
    {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setUnlocalizedName(ElementsOfPower.MODID + ".magicOrb");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public int getMetadata(int damageValue)
    {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length)
        {
            sub = 0;
        }

        return getUnlocalizedName() + "." + subNames[sub];
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int meta = 0; meta < subNames.length; meta++)
        {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("text." + ElementsOfPower.MODID + ".magicOrb.use"));
        tooltip.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("text." + ElementsOfPower.MODID + ".magicOrb.cocoon"));
    }

    // CUSTOM STUFF
    public ItemStack getStack(int count, int damageValue)
    {
        ItemStack stack = new ItemStack(this, count);
        stack.setItemDamage(damageValue);
        return stack;
    }
}
