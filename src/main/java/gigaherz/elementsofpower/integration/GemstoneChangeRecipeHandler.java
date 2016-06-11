package gigaherz.elementsofpower.integration;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class GemstoneChangeRecipeHandler implements IRecipeHandler<GemstoneChangeRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<GemstoneChangeRecipeWrapper> getRecipeClass()
    {
        return GemstoneChangeRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull GemstoneChangeRecipeWrapper recipe)
    {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull GemstoneChangeRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull GemstoneChangeRecipeWrapper recipe)
    {
        return true;
    }
}
