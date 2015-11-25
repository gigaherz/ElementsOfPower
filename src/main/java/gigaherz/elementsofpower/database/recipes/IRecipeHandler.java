package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.crafting.IRecipe;

public interface IRecipeHandler
{
    boolean accepts(IRecipe recipe);
    IRecipeInfoProvider handle(IRecipe recipe);
}
