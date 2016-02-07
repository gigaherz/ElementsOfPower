package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IRecipeInfoProvider
{
    @Nonnull
    ItemStack getRecipeOutput();

    @Nonnull
    List<ItemStack> getRecipeInputs();
}
