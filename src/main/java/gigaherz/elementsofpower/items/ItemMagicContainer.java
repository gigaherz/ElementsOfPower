package gigaherz.elementsofpower.items;

import gigaherz.common.state.ItemStateful;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public abstract class ItemMagicContainer extends ItemStateful
{
    public ItemMagicContainer(String name)
    {
        super(name);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    public boolean isInfinite(ItemStack stack)
    {
        return false;
    }

    public ItemStack getStack(Gemstone gemstone)
    {
        return getStack(1, gemstone);
    }

    public ItemStack getStack(int count, Gemstone gemstone)
    {
        return new ItemStack(this, count, gemstone.ordinal());
    }

    public abstract MagicAmounts getCapacity(ItemStack stack);

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        if (isInfinite(stack))
            return true;

        MagicAmounts amounts = ContainerInformation.getContainedMagic(stack);

        return !amounts.isEmpty();
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltipList, boolean showAdvancedInfo)
    {
        MagicAmounts amounts = ContainerInformation.getContainedMagic(stack);

        if (amounts.isEmpty())
        {
            return;
        }

        tooltipList.add(TextFormatting.YELLOW + "Contains magic:");
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            tooltipList.add(TextFormatting.GRAY + "  (Hold SHIFT)");
            return;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.get(i) == 0)
            {
                continue;
            }

            String magicName = MagicAmounts.getMagicName(i);
            String str;
            if (ContainerInformation.isInfiniteContainer(stack))
                str = String.format("%s  %s x\u221E", TextFormatting.GRAY, magicName);
            else
                str = String.format("%s  %s x%s", TextFormatting.GRAY, magicName,
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.get(i)));
            tooltipList.add(str);
        }
    }

    public ItemStack addContainedMagic(ItemStack stack, ItemStack orb)
    {
        if (stack.getCount() <= 0)
            return ItemStack.EMPTY;
        if (orb.getCount() <= 0)
            return stack;
        MagicAmounts am = MagicAmounts.EMPTY;
        am = am.add(ContainerInformation.getContainedMagic(stack));
        am = am.add(Element.values[orb.getMetadata()], 8);

        MagicAmounts lm = ContainerInformation.getMagicLimits(stack);
        if (!lm.isEmpty())
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                if (lm.get(i) < am.get(i))
                    return ItemStack.EMPTY;
            }
        }

        return ContainerInformation.setContainedMagic(stack, am);
    }
}
