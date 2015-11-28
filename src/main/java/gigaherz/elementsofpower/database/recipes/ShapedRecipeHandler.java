package gigaherz.elementsofpower.database.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.List;

public class ShapedRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(IRecipe recipe)
    {
        return recipe instanceof ShapedRecipes;
    }

    @Override
    public IRecipeInfoProvider handle(IRecipe recipe)
    {
        return new ShapedRecipeInfo((ShapedRecipes) recipe);
    }

    private static class ShapedRecipeInfo implements IRecipeInfoProvider
    {
        ArrayList<ItemStack> recipeItems = new ArrayList<>();
        ShapedRecipes recipe;

        public ShapedRecipeInfo(ShapedRecipes recipe)
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

        @Override
        public ItemStack getRecipeOutput()
        {
            return recipe.getRecipeOutput();
        }

        @Override
        public List<ItemStack> getRecipeInputs()
        {
            return recipeItems;
        }
    }
}