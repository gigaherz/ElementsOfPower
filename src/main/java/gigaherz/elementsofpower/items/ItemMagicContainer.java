package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public abstract class ItemMagicContainer extends Item
{
    public ItemMagicContainer()
    {
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    public boolean isInfinite(ItemStack stack)
    {
        return false;
    }

    public ItemStack getStack(int count, int damageValue)
    {
        return new ItemStack(this, count, damageValue);
    }

    public abstract MagicAmounts getCapacity(ItemStack stack);

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        if (isInfinite(stack))
            return true;

        MagicAmounts amounts = ContainerInformation.getContainedMagic(stack);

        return amounts != null && !amounts.isEmpty();
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltipList, boolean showAdvancedInfo)
    {
        MagicAmounts amounts = ContainerInformation.getContainedMagic(stack);

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

            String magicName = MagicAmounts.getMagicName(i);
            String str;
            if (ContainerInformation.isInfiniteContainer(stack))
                str = String.format("%s  %s x\u221E", EnumChatFormatting.GRAY, magicName);
            else
                str = String.format("%s  %s x%s", EnumChatFormatting.GRAY, magicName,
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.amounts[i]));
            tooltipList.add(str);
        }
    }

    public ItemStack addContainedMagic(ItemStack stack, ItemStack orb)
    {
        if (stack == null)
            return null;
        if (orb == null)
            return stack;
        MagicAmounts am = new MagicAmounts();
        am.add(ContainerInformation.getContainedMagic(stack));
        am.element(Element.values()[orb.getMetadata()], 8);

        MagicAmounts lm = ContainerInformation.getMagicLimits(stack);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (lm.amounts[i] < am.amounts[i])
                return null;
        }

        return ContainerInformation.setContainedMagic(stack, am);
    }
}
