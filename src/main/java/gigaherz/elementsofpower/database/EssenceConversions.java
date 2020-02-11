package gigaherz.elementsofpower.database;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class EssenceConversions
{
    public static Map<Item, MagicAmounts> itemEssences = Maps.newHashMap();

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
                ElementsOfPowerMod.logger.warn("StackSize is invalid! " + output.toString());
                continue;
            }

            if (output.getCount() > 1)
            {
                output = output.copy();
                output.setCount(1);
            }

            if (itemHasEssence(output.getItem()))
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
                am = am.multiply(1.0f / count);
            }

            addConversion(output.getItem(), am);
        }
    }

    public static boolean itemHasEssence(Item item)
    {
        return itemEssences.containsKey(item);
    }

    public static MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
    {
        int count = stack.getCount();
        if (count > 1)
        {
            stack = stack.copy();
            stack.setCount(1);
        }

        MagicAmounts m = getEssences(stack.getItem());

        if (count > 1 && wholeStack)
        {
            m = m.multiply(count);
        }

        return m;
    }

    public static MagicAmounts getEssences(Item stack)
    {
        return itemEssences.getOrDefault(stack, MagicAmounts.EMPTY);
    }

    public static void addConversion(Item item, MagicAmounts amounts)
    {
        if (itemEssences.containsKey(item))
        {
            ElementsOfPowerMod.logger.error("Stack already inserted! " + item.toString());
            return;
        }

        itemEssences.put(item, amounts);
    }
}
