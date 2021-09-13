package dev.gigaherz.elementsofpower.misc;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public interface IMultiOutputRecipe<T extends Container> extends Recipe<T>
{
    // GOOD COMMENT:

    /* Provide a default implementation so that subclasses don't need to implement this exception themselves.
     */
    @Override
    default ItemStack assemble(T inv)
    {
        throw new RuntimeException("This recipe has multiple outputs. Use getCraftingResults instead.");
    }

    NonNullList<ItemStack> getCraftingResults(T context);
}
