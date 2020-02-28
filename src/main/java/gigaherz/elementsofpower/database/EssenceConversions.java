package gigaherz.elementsofpower.database;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public class EssenceConversions
{
    public static final EssenceConversions CLIENT = new EssenceConversions();
    public static final EssenceConversions SERVER = new EssenceConversions();

    public static EssenceConversions get(@Nullable World world)
    {
        return (world != null && world.isRemote) ? CLIENT : SERVER;
    }

    private final Map<Item, MagicAmounts> essenceMappings = Maps.newHashMap();

    public boolean itemHasEssence(Item item)
    {
        return essenceMappings.containsKey(item);
    }

    public MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
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

    public MagicAmounts getEssences(Item stack)
    {
        return essenceMappings.getOrDefault(stack, MagicAmounts.EMPTY);
    }

    public void addConversion(Item item, MagicAmounts amounts)
    {
        if (essenceMappings.containsKey(item))
        {
            ElementsOfPowerMod.LOGGER.error("Stack already inserted! " + item.toString());
            return;
        }

        essenceMappings.put(item, amounts);
    }

    public Map<Item, MagicAmounts> getAllConversions()
    {
        return Collections.unmodifiableMap(essenceMappings);
    }

    public void clear()
    {
        essenceMappings.clear();
    }

    public void receiveFromServer(Map<Item, MagicAmounts> data)
    {
        essenceMappings.clear();
        essenceMappings.putAll(data);
    }

    public static void registerEssencesForRecipes()
    {
        Map<Item, RecipeTools.ItemSource> itemSources = RecipeTools.gatherRecipes();

        for (Map.Entry<Item, RecipeTools.ItemSource> it : itemSources.entrySet())
        {
            Item output = it.getKey();
            RecipeTools.ItemSource inputs = it.getValue();

            float count = inputs.numProduced;
            if (count < 1)
            {
                ElementsOfPowerMod.LOGGER.warn("StackSize is invalid! " + output.toString());
                continue;
            }

            if (SERVER.itemHasEssence(output))
                continue;

            boolean allFound = true;
            MagicAmounts am = MagicAmounts.EMPTY;
            for (ItemStack b : inputs.sources)
            {
                MagicAmounts m = SERVER.getEssences(b, true);

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

            SERVER.addConversion(output.getItem(), am);
        }
    }
}
