package gigaherz.elementsofpower.database.recipes.crafting;

import gigaherz.elementsofpower.database.recipes.IRecipeHandler;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShapelessRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(@Nonnull IRecipe recipe)
    {
        return recipe.getRecipeOutput() != null && recipe instanceof ShapelessRecipes;
    }

    @Nonnull
    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe recipe)
    {
        return new RecipeInfo((ShapelessRecipes) recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {

        ArrayList<ItemStack> recipeItems = new ArrayList<>();
        ShapelessRecipes recipe;

        public RecipeInfo(ShapelessRecipes recipe)
        {
            this.recipe = recipe;

            List<ItemStack> inputs = recipe.recipeItems;
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