package gigaherz.elementsofpower.database.recipes;

import javax.annotation.Nonnull;

public interface IRecipeInfoConsumer
{
    void process(@Nonnull IRecipeInfoProvider provider);
}
