package gigaherz.elementsofpower.misc;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;

public interface IMultiOutputRecipe<T extends IInventory> extends IRecipe<T>
{
    // GOOD COMMENT:

    /* Provide a default implementation so that subclasses don't need to implement this exception themselves.
     */
    @Override
    default ItemStack getCraftingResult(T inv) {
        throw new RuntimeException("This recipe has multiple outputs. Use getCraftingResults instead.");
    }

    NonNullList<ItemStack> getCraftingResults(T context);
}
