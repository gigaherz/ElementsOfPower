package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.ItemRegistered;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemMagicContainer extends ItemRegistered
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

    @Nullable
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
            if (amounts.amounts[i] == 0)
            {
                continue;
            }

            String magicName = MagicAmounts.getMagicName(i);
            String str;
            if (ContainerInformation.isInfiniteContainer(stack))
                str = String.format("%s  %s x\u221E", TextFormatting.GRAY, magicName);
            else
                str = String.format("%s  %s x%s", TextFormatting.GRAY, magicName,
                        ElementsOfPower.prettyNumberFormatter2.format(amounts.amounts[i]));
            tooltipList.add(str);
        }
    }

    @Nullable
    public ItemStack addContainedMagic(@Nullable ItemStack stack, @Nullable ItemStack orb)
    {
        if (stack == null)
            return null;
        if (orb == null)
            return stack;
        MagicAmounts am = new MagicAmounts();
        am.add(ContainerInformation.getContainedMagic(stack));
        am.element(Element.values[orb.getMetadata()], 8);

        MagicAmounts lm = ContainerInformation.getMagicLimits(stack);
        if (!lm.isEmpty())
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                if (lm.amounts[i] < am.amounts[i])
                    return null;
            }
        }

        return ContainerInformation.setContainedMagic(stack, am);
    }
}
