package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IConversionCache
{
    boolean hasEssences(ItemStack stack);
    MagicAmounts getEssences(ItemStack item, boolean wholeStack);

    default boolean hasEssences(Item item)
    {
        return hasEssences(new ItemStack(item));
    }

    default MagicAmounts getEssences(Item item)
    {
        return getEssences(new ItemStack(item), false);
    }
}
