package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nonnull;

public interface IRecipeHandler
{
    boolean accepts(@Nonnull IRecipe recipe);

    @Nonnull
    IRecipeInfoProvider handle(@Nonnull IRecipe recipe);
}
