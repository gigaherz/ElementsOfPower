package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemMagicContainer extends Item
{
    private static final String[] subNames = {
            ".lapisContainer", ".emeraldContainer", ".diamondContainer"
    };

    public ItemMagicContainer()
    {
        setMaxStackSize(1);
        setUnlocalizedName(ElementsOfPower.MODID + ".magicContainer");
    }

    public boolean isInfinite(ItemStack stack)
    {
        return false;
    }

    public ItemStack getStack(int count, int damageValue)
    {
        return new ItemStack(this, count, damageValue);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length)
        {
            return getUnlocalizedName();
        }

        return "item." + ElementsOfPower.MODID + subNames[sub];
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
    public boolean hasEffect(ItemStack stack)
    {
        if (isInfinite(stack))
            return true;

        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        return amounts != null && !amounts.isEmpty();
    }

    @Override
    public EnumRarity getRarity(ItemStack stack)
    {

        if (hasEffect(stack))
            return EnumRarity.RARE;
        return EnumRarity.UNCOMMON;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltipList, boolean showAdvancedInfo)
    {
        MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

        if (amounts == null)
        {
            return;
        }

        tooltipList.add(EnumChatFormatting.YELLOW + "Contains magic:");
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            tooltipList.add(EnumChatFormatting.GRAY + "  (Hold SHIFT)");
            return;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.amounts[i] == 0)
            {
                continue;
            }

            String magicName = MagicDatabase.getMagicName(i);
            String str;
            if (MagicDatabase.isInfiniteContainer(stack))
                str = String.format("%s  %s x\u221E", EnumChatFormatting.GRAY, magicName);
            else
                str = String.format("%s  %s x%s", EnumChatFormatting.GRAY, magicName,
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.amounts[i]));
            tooltipList.add(str);
        }
    }
}
