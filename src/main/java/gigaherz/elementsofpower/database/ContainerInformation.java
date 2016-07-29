package gigaherz.elementsofpower.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.elementsofpower.ElementsOfPower;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ContainerInformation
{
    static Random rand = new Random();

    @Nullable
    public static ItemStack identifyQuality(@Nullable ItemStack stack)
    {
        if (stack == null)
            return null;

        Item item = stack.getItem();
        if (item instanceof ItemGemstone)
        {
            if (((ItemGemstone) item).getQuality(stack) != null)
                return stack;
        }

        @SuppressWarnings("unchecked")
        List<Pair<ItemStack, String>> gems = Lists.newArrayList(
                Pair.of(ElementsOfPower.gemRuby, "gemRuby"),
                Pair.of(ElementsOfPower.gemSapphire, "gemSapphire"),
                Pair.of(ElementsOfPower.gemCitrine, "gemCitrine"),
                Pair.of(ElementsOfPower.gemAgate, "gemAgate"),
                Pair.of(ElementsOfPower.gemQuartz, "gemQuartz"),
                Pair.of(ElementsOfPower.gemSerendibite, "gemSerendibite"),
                Pair.of(ElementsOfPower.gemEmerald, "gemEmerald"),
                Pair.of(ElementsOfPower.gemAmethyst, "gemAmethyst"),
                Pair.of(ElementsOfPower.gemDiamond, "gemDiamond")
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
        if (stack.stackSize != 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
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
        if (stack.stackSize != 1)
            return MagicAmounts.empty();

        Item item = stack.getItem();
        if (!(item instanceof ItemMagicContainer))
            return MagicAmounts.empty();

        MagicAmounts m = ((ItemMagicContainer) item).getCapacity(stack);
        if (m == null)
            return MagicAmounts.empty();

        return m.copy();
    }

    public static MagicAmounts getContainedMagic(@Nullable ItemStack output)
    {
        if (output == null)
        {
            return MagicAmounts.empty();
        }

        if (output.stackSize != 1)
        {
            return MagicAmounts.empty();
        }

        if (isInfiniteContainer(output))
            return new MagicAmounts().all(999);

        if (!(output.getItem() instanceof ItemMagicContainer))
            return MagicAmounts.empty();

        NBTTagCompound nbt = output.getTagCompound();

        if (nbt == null)
        {
            return MagicAmounts.empty();
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

        return MagicAmounts.empty();
    }

    @Nullable
    public static ItemStack setContainedMagic(@Nullable ItemStack output, MagicAmounts amounts)
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

        if (!amounts.isEmpty())
        {
            output = identifyQuality(output);
            if (output == null)
                return null;

            NBTTagCompound nbt = output.getTagCompound();
            if (nbt == null)
            {
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

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (self.amounts[i] > 0 && amounts.amounts[i] < limits.amounts[i])
                return true;
        }

        return false;
    }

    public static boolean isInfiniteContainer(@Nullable ItemStack stack)
    {
        if (stack == null)
            return false;
        Item item = stack.getItem();
        return item instanceof ItemMagicContainer
                && ((ItemMagicContainer) item).isInfinite(stack);
    }
}
