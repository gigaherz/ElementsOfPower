package gigaherz.elementsofpower.database;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;

public class Utils
{
    public static <OType> boolean stackMapContainsKey(Map<ItemStack, OType> map, ItemStack stack)
    {
        for (ItemStack k : map.keySet())
        {
            if (OreDictionary.itemMatches(stack, k, false))
            {
                return true;
            }
        }

        return false;
    }

    public static <OType> OType stackMapGet(Map<ItemStack, OType> map, ItemStack stack)
    {
        for (Map.Entry<ItemStack, OType> entry : map.entrySet())
        {
            if (OreDictionary.itemMatches(stack, entry.getKey(), false))
            {
                return entry.getValue();
            }
        }

        return null;
    }

    public static int gcd(int a, int b)
    {
        for (; ; )
        {
            if (a == 0) return b;
            b %= a;
            if (b == 0) return a;
            a %= b;
        }
    }

    public static int lcm(int a, int b)
    {
        int temp = gcd(a, b);
        return temp > 0 ? (a / temp * b) : 0;
    }
}
