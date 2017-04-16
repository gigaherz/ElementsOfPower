package gigaherz.elementsofpower.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.ItemGemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class ContainerInformation
{
    static Random rand = new Random();

    public static ItemStack identifyQuality(ItemStack stack)
    {
        if (stack.getCount() <= 0)
            return ItemStack.EMPTY;

        Item item = stack.getItem();
        if (item instanceof ItemGemstone)
        {
            if (((ItemGemstone) item).getQuality(stack) != null)
                return stack;
        }

        @SuppressWarnings("unchecked")
        List<Pair<ItemStack, String>> gems = Lists.newArrayList(
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Ruby), "gemRuby"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Sapphire), "gemSapphire"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Citrine), "gemCitrine"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Agate), "gemAgate"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Quartz), "gemQuartz"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Serendibite), "gemSerendibite"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Emerald), "gemEmerald"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Amethyst), "gemAmethyst"),
                Pair.of(ElementsOfPower.gemstone.getStack(Gemstone.Diamond), "gemDiamond")
        );

        int[] ids = OreDictionary.getOreIDs(stack);
        Set<String> names = Sets.newHashSet();
        for (int i : ids)
        { names.add(OreDictionary.getOreName(i)); }

        for (Pair<ItemStack, String> target : gems)
        {
            if (names.contains(target.getRight()))
            {
                return setRandomQualityVariant(target.getLeft().copy());
            }
        }

        return stack;
    }

    private static ItemStack setRandomQualityVariant(ItemStack target)
    {
        float rnd = rand.nextFloat();
        if (rnd > 0.3f)
            return ElementsOfPower.gemstone.setQuality(target, Quality.Rough);
        if (rnd > 0.1f)
            return ElementsOfPower.gemstone.setQuality(target, Quality.Common);
        if (rnd > 0.01f)
            return ElementsOfPower.gemstone.setQuality(target, Quality.Smooth);
        if (rnd > 0.001f)
            return ElementsOfPower.gemstone.setQuality(target, Quality.Flawless);

        return ElementsOfPower.gemstone.setQuality(target, Quality.Pure);
    }

    public static boolean canItemContainMagic(ItemStack stack)
    {
        if (stack.getCount() != 1)
        {
            stack = stack.copy();
            stack.setCount(1);
        }
        return !getMagicLimits(stack).isEmpty();
    }

    public static boolean itemContainsMagic(ItemStack stack)
    {
        if (isInfiniteContainer(stack))
            return true;

        MagicAmounts amounts = getContainedMagic(stack);

        return !amounts.isEmpty();
    }

    public static MagicAmounts getMagicLimits(ItemStack stack)
    {
        if (stack.getCount() != 1)
            return MagicAmounts.EMPTY;

        Item item = stack.getItem();
        if (!(item instanceof ItemMagicContainer))
            return MagicAmounts.EMPTY;

        return ((ItemMagicContainer) item).getCapacity(stack);
    }

    public static MagicAmounts getContainedMagic(ItemStack output)
    {
        if (output.getCount() != 1)
        {
            return MagicAmounts.EMPTY;
        }

        if (isInfiniteContainer(output))
            return MagicAmounts.EMPTY.all(999);

        if (!(output.getItem() instanceof ItemMagicContainer))
            return MagicAmounts.EMPTY;

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null)
        {
            return MagicAmounts.EMPTY;
        }

        MagicAmounts amounts = MagicAmounts.EMPTY;
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

                amounts = amounts.with(i, amount);
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

        return MagicAmounts.EMPTY;
    }

    public static ItemStack setContainedMagic(ItemStack output, MagicAmounts amounts)
    {
        if (output.getCount() != 1)
        {
            return output;
        }

        if (isInfiniteContainer(output))
            return output;

        if (!amounts.isEmpty())
        {
            output = identifyQuality(output);
            if (output.getCount() <= 0)
                return ItemStack.EMPTY;

            NBTTagCompound nbt = output.getTagCompound();
            if (nbt == null)
            {
                nbt = new NBTTagCompound();
                output.setTagCompound(nbt);
            }

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                nbt.setFloat("" + i, amounts.get(i));
            }

            return output;
        }
        else
        {
            NBTTagCompound nbt = output.getTagCompound();

            if (nbt != null)
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    nbt.removeTag("" + i);
                }
            }

            return output;
        }
    }

    public static boolean isContainerFull(ItemStack stack)
    {
        MagicAmounts limits = getMagicLimits(stack);
        MagicAmounts amounts = getContainedMagic(stack);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts.get(i) < limits.get(i))
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

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (self.get(i) > 0 && amounts.get(i) < limits.get(i))
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
