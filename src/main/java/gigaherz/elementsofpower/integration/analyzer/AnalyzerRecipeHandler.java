package gigaherz.elementsofpower.integration.analyzer;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;

public class AnalyzerRecipeHandler implements IRecipeHandler<AnalyzerRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<AnalyzerRecipeWrapper> getRecipeClass()
    {
        return AnalyzerRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return AnalyzerCategory.UID;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull AnalyzerRecipeWrapper recipe)
    {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull AnalyzerRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull AnalyzerRecipeWrapper recipe)
    {
        return true;
    }
}
