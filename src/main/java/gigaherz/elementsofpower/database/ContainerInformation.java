package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import java.util.HashMap;
import java.util.Map;

public class ContainerInformation
{
    public static Map<ItemStack, ItemStack> containerConversion = new HashMap<>();
    public static Map<ItemStack, ItemStack> containerConversionReverse = new HashMap<>();
    public static Map<ItemStack, MagicAmounts> containerCapacity = new HashMap<>();

    public static void reverseContainerConversions()
    {
        for(Map.Entry<ItemStack,ItemStack> conversion : containerConversion.entrySet())
        {
            containerConversionReverse.put(conversion.getValue(), conversion.getKey());
        }
    }

    public static void registerContainerCapacity()
    {
        containerCapacity.put(new ItemStack(Items.dye, 1, 4), new MagicAmounts().all(10));
        containerCapacity.put(new ItemStack(Items.emerald, 1), new MagicAmounts().all(50));
        containerCapacity.put(new ItemStack(Items.diamond, 1), new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.containerLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.containerEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.containerDiamond, new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.wandLapis, new MagicAmounts().all(10));
        containerCapacity.put(ElementsOfPower.wandEmerald, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.wandDiamond, new MagicAmounts().all(100));

        containerCapacity.put(ElementsOfPower.staffLapis, new MagicAmounts().all(50));
        containerCapacity.put(ElementsOfPower.staffEmerald, new MagicAmounts().all(250));
        containerCapacity.put(ElementsOfPower.staffDiamond, new MagicAmounts().all(500));

        containerCapacity.put(ElementsOfPower.ringLapis, new MagicAmounts().all(25));
        containerCapacity.put(ElementsOfPower.ringEmerald, new MagicAmounts().all(100));
        containerCapacity.put(ElementsOfPower.ringDiamond, new MagicAmounts().all(250));
    }

    public static void registerContainerConversions()
    {
        containerConversion.put(new ItemStack(Items.dye, 1, 4), ElementsOfPower.containerLapis);
        containerConversion.put(new ItemStack(Items.emerald, 1), ElementsOfPower.containerEmerald);
        containerConversion.put(new ItemStack(Items.diamond, 1), ElementsOfPower.containerDiamond);
    }

    public static boolean canItemContainMagic(ItemStack stack)
    {
        if (stack.stackSize != 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        return Utils.stackMapContainsKey(containerCapacity, stack);
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        if (isInfiniteContainer(stack))
            return true;

        MagicAmounts amounts = getContainedMagic(stack);

        return amounts != null && !amounts.isEmpty();
    }

    public static MagicAmounts getMagicLimits(ItemStack stack)
    {
        if (stack.stackSize != 1)
        {
            return null;
        }

        MagicAmounts m = Utils.stackMapGet(containerCapacity, stack);
        if (m == null)
            return null;

        return m.copy();
    }

    public static MagicAmounts getContainedMagic(ItemStack output)
    {
        if (output == null)
        {
            return null;
        }

        if (output.stackSize != 1)
        {
            return null;
        }

        if (isInfiniteContainer(output))
            return new MagicAmounts().all(999);

        if (!(output.getItem() instanceof ItemMagicContainer))
            return null;

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null)
        {
            return null;
        }

        MagicAmounts amounts = new MagicAmounts();
        float max = 0;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            try
            {
                float amount = nbt.getFloat("" + i);

                if (amount > max)
                {
                    max = amount;
                }

                amounts.amounts[i] = amount;
            }
            catch (NumberFormatException ex)
            {
                throw new ReportedException(new CrashReport("Exception while parsing NBT magic infromation", ex));
            }
        }

        if (max > 0)
        {
            return amounts;
        }

        return null;
    }

    public static ItemStack setContainedMagic(ItemStack output, MagicAmounts amounts)
    {
        if (output == null)
        {
            return null;
        }

        if (output.stackSize != 1)
        {
            return null;
        }

        if (isInfiniteContainer(output))
            return output;

        if (amounts != null)
        {
            if (amounts.isEmpty())
            {
                amounts = null;
            }
        }

        if (amounts != null)
        {
            NBTTagCompound nbt = output.getTagCompound();

            if (nbt == null)
            {
                ItemStack output2 = Utils.stackMapGet(containerConversion, output);
                if (output2 != null)
                {
                    output = output2.copy();
                }

                nbt = new NBTTagCompound();
                output.setTagCompound(nbt);
            }

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                nbt.setFloat("" + i, amounts.amounts[i]);
            }

            return output;
        }
        else
        {
            output.setTagCompound(null);

            ItemStack output2 = Utils.stackMapGet(containerConversion, output);
            if (output2 != null)
            {
                output = output2.copy();
            }

            return output;
        }
    }

    public static boolean isContainerFull(ItemStack stack)
    {
        MagicAmounts limits = getMagicLimits(stack);
        MagicAmounts amounts = getContainedMagic(stack);

        if (amounts == null)
            return false;

        if (limits == null)
            return true;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.amounts[i] < limits.amounts[i])
                return false;
        }

        return true;
    }

    public static boolean canTransferAnything(ItemStack stack, MagicAmounts self)
    {
        if (isInfiniteContainer(stack))
            return false;

        MagicAmounts limits = getMagicLimits(stack);
        MagicAmounts amounts = getContainedMagic(stack);

        if (limits == null)
            return true;

        if (amounts == null)
            return true;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (self.amounts[i] > 0 && amounts.amounts[i] < limits.amounts[i])
                return true;
        }

        return false;
    }

    public static boolean isInfiniteContainer(ItemStack stack)
    {
        Item item = stack.getItem();
        return item instanceof ItemMagicContainer
                && ((ItemMagicContainer) item).isInfinite(stack);
    }
}
