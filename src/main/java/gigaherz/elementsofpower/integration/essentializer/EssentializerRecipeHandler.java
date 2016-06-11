package gigaherz.elementsofpower.integration.essentializer;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;

public class EssentializerRecipeHandler implements IRecipeHandler<EssentializerRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<EssentializerRecipeWrapper> getRecipeClass()
    {
        return EssentializerRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return EssentializerCategory.UID;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull EssentializerRecipeWrapper recipe)
    {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull EssentializerRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull EssentializerRecipeWrapper recipe)
    {
        return true;
    }
}
