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

            int stackSize = output.stackSize;
            if (stackSize < 1)
            {
                ElementsOfPower.logger.warn("StackSize is invalid! " + output.toString());
                continue;
            }

            if (output.stackSize > 1)
            {
                output = output.copy();
                output.stackSize = 1;
            }

            if (itemHasEssence(output))
                continue;

            boolean allFound = true;
            MagicAmounts am = new MagicAmounts();
            for (ItemStack b : inputs)
            {
                MagicAmounts m = getEssences(b, true);

                if (m == null || m.isEmpty())
                {
                    allFound = false;
                    break;
                }

                am.add(m);
            }

            if (!allFound)
                continue;

            if (stackSize > 1)
            {
                for (int i = 0; i < am.amounts.length; i++)
                {
                    am.amounts[i] /= stackSize;
                }
            }

            addConversion(output, am);
        }
    }

    public static boolean itemHasEssence(ItemStack stack)
    {
        if (stack.stackSize > 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        return Utils.stackMapContainsKey(itemEssences, stack);
    }

    public static MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
    {
        int stackSize = stack.stackSize;
        if (stackSize > 1)
        {
            stack = stack.copy();
            stack.stackSize = 1;
        }

        MagicAmounts m = Utils.stackMapGet(itemEssences, stack);
        if (m == null)
            return null;

        m = m.copy();

        if (stackSize > 1 && wholeStack)
        {
            for (int i = 0; i < m.amounts.length; i++)
            {
                m.amounts[i] *= stackSize;
            }
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
