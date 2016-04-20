package gigaherz.elementsofpower.database.recipes.crafting;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.database.recipes.IRecipeHandler;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShapedRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(@Nonnull IRecipe recipe)
    {
        return recipe instanceof ShapedRecipes;
    }

    @Nonnull
    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe recipe)
    {
        return new RecipeInfo((ShapedRecipes) recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {
        ArrayList<ItemStack> recipeItems = Lists.newArrayList();
        ShapedRecipes recipe;

        public RecipeInfo(ShapedRecipes recipe)
        {
            this.recipe = recipe;

            ItemStack[] inputs = recipe.recipeItems;
            for (ItemStack input : inputs)
            {
                if (input == null)
                    continue;

                ItemStack stack = input.copy();
                stack.stackSize = 1;
                recipeItems.add(stack);
            }
        }

        @Nonnull
        @Override
        public ItemStack getRecipeOutput()
        {
            return recipe.getRecipeOutput();
        }

        @Nonnull
        @Override
        public List<ItemStack> getRecipeInputs()
        {
            return recipeItems;
        }
    }
}