package gigaherz.elementsofpower.integration;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class ContainerChargeRecipeHandler implements IRecipeHandler<ContainerChargeRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<ContainerChargeRecipeWrapper> getRecipeClass()
    {
        return ContainerChargeRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull ContainerChargeRecipeWrapper recipe)
    {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull ContainerChargeRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull ContainerChargeRecipeWrapper recipe)
    {
        return true;
    }
}
