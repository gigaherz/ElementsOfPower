package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.List;

public interface IRecipeInfoProvider
{
    @Nonnull
    ItemStack getRecipeOutput();

    @Nonnull
    List<ScaledIngredient> getRecipeInputs();
}
