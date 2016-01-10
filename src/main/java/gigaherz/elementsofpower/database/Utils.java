package gigaherz.elementsofpower.database;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Map;

public class Utils
{
    public static ItemStack getExistingInList(List<ItemStack> list, ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

        for (ItemStack k : list)
        {
            if (stackFitsInSlot(stack, k))
            {
                return k;
            }
        }

        return null;
    }

    public static <OType> OType getFromMap(Map<ItemStack, OType> map, ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

        for (ItemStack k : map.keySet())
        {
            if (stackFitsInSlot(stack, k))
            {
                OType t = map.get(k);
                return t;
            }
        }

        return null;
    }

    public static <OType> boolean stackIsInMap(Map<ItemStack, OType> map, ItemStack stack)
    {
        return stack != null && getFromMap(map, stack) != null;
    }

    public static ItemStack findKeyForValue(Map<ItemStack, ItemStack> map, ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

        for (ItemStack k : map.keySet())
        {
            ItemStack v = map.get(k);

            if (compareItemStacksStrict(v, stack))
            {
                return k;
            }
        }

        return null;
    }

    public static boolean stackFitsInSlot(ItemStack test, ItemStack stack)
    {
        int dmg = test.getItemDamage();
        return test.getItem() == stack.getItem() && (dmg == OreDictionary.WILDCARD_VALUE || dmg == stack.getItemDamage());
    }

    public static boolean compareItemStacksStrict(ItemStack test, ItemStack stack)
    {
        return test.getItem() == stack.getItem() && test.getItemDamage() == stack.getItemDamage();
    }
}
