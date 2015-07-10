package gigaherz.elementsofpower;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSheep1;
import net.minecraft.item.ItemStack;

import java.util.*;

public class Utils {

    public static ItemStack getExistingInList(List<ItemStack> list, ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (ItemStack k : list) {
            if (compareItemStacksStrict(stack, k)) {
                return k;
            }
        }

        return null;
    }

    public static <OType> OType getFromMap(Map<ItemStack, OType> map, ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (ItemStack k : map.keySet()) {
            if (compareItemStacksStrict(stack, k)) {
                return map.get(k);
            }
        }

        return null;
    }

    public static <OType> boolean stackIsInMap(Map<ItemStack, OType> map, ItemStack stack) {
        return stack != null && getFromMap(map, stack) != null;

    }

    public static ItemStack findKeyForValue(Map<ItemStack, ItemStack> map, ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (ItemStack k : map.keySet()) {
            ItemStack v = map.get(k);

            if (compareItemStacksStrict(v, stack)) {
                return k;
            }
        }

        return null;
    }

    public static boolean stackFitsInSlot(ItemStack test, ItemStack stack) {
        int dmg = test.getItemDamage();
        return test.getItem() == stack.getItem() && (dmg < 0 || dmg >= 32767 || dmg == stack.getItemDamage());
    }

    public static boolean compareItemStacksStrict(ItemStack test, ItemStack stack) {
        return test.getItem() == stack.getItem() && test.getItemDamage() == stack.getItemDamage();
    }
}
