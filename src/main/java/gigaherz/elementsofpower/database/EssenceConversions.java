package gigaherz.elementsofpower.database;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class EssenceConversions
{
    public static Map<ItemStack, MagicAmounts> itemEssences = Maps.newHashMap();

    public static void registerEssencesForRecipes()
    {
        Map<ItemStack, List<ItemStack>> itemSources = RecipeTools.gatherRecipes();

        for (Map.Entry<ItemStack, List<ItemStack>> it : itemSources.entrySet())
        {
            ItemStack output = it.getKey();
            List<ItemStack> inputs = it.getValue();

            int count = output.getCount();
            if (count < 1)
            {
                ElementsOfPower.logger.warn("StackSize is invalid! " + output.toString());
                continue;
            }

            if (output.getCount() > 1)
            {
                output = output.copy();
                output.setCount(1);
            }

            if (itemHasEssence(output))
                continue;

            boolean allFound = true;
            MagicAmounts am = MagicAmounts.EMPTY;
            for (ItemStack b : inputs)
            {
                MagicAmounts m = getEssences(b, true);

                if (m.isEmpty())
                {
                    allFound = false;
                    break;
                }

                am = am.add(m);
            }

            if (!allFound)
                continue;

            if (count > 1)
            {
                am = am.multiply(1 / count);
            }

            addConversion(output, am);
        }
    }

    public static boolean itemHasEssence(ItemStack stack)
    {
        if (stack.getCount() > 1)
        {
            stack = stack.copy();
            stack.setCount(1);
        }
        return Utils.stackMapContainsKey(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
    {
        int count = stack.getCount();
        if (count > 1)
        {
            stack = stack.copy();
            stack.setCount(1);
        }

        MagicAmounts m = Utils.stackMapGet(itemEssences, stack, MagicAmounts.EMPTY);

        if (count > 1 && wholeStack)
        {
            m = m.multiply(count);
        }

        return m;
    }

    public static void addConversion(ItemStack item, MagicAmounts amounts)
    {
        if (Utils.stackMapContainsKey(itemEssences, item))
        {
            ElementsOfPower.logger.error("Stack already inserted! " + item.toString());
            return;
        }

        itemEssences.put(item, amounts);
    }
}
