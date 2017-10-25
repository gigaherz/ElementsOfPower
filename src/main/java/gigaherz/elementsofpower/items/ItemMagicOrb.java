package gigaherz.elementsofpower.items;

import gigaherz.common.ItemRegistered;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMagicOrb extends ItemRegistered
{
    private final static String[] subNames =
            {"fire", "water", "air", "earth", "light", "darkness", "life", "death"};

    public ItemMagicOrb(String name)
    {
        super(name);
        setMaxStackSize(64);
        setHasSubtypes(true);
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
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        for (int meta = 0; meta < subNames.length; meta++)
        {
            subItems.add(new ItemStack(this, 1, meta));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".orb.use"));
        tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("text." + ElementsOfPower.MODID + ".orb.cocoon"));
    }

    public ItemStack getStack(Element element)
    {
        return getStack(1, element);
    }

    public ItemStack getStack(int count, Element element)
    {
        return new ItemStack(this, count, element.ordinal());
    }
}
