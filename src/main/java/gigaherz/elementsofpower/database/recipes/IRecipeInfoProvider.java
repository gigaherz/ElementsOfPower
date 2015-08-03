package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface IRecipeInfoProvider
{
    ItemStack getRecipeOutput();

    List<ItemStack> getRecipeInputs();
}
