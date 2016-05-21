package gigaherz.elementsofpower.database.recipes.crafting;

import com.google.common.collect.Lists;
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
        return recipe instanceof ShapelessRecipes;
    }

    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe recipe)
    {
        return new RecipeInfo((ShapelessRecipes) recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {
        ArrayList<ItemStack> recipeItems = Lists.newArrayList();
        ItemStack output;

        public RecipeInfo(ShapelessRecipes recipe)
        {
            output = recipe.getRecipeOutput();

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

        @Override
        public ItemStack getRecipeOutput()
        {
            return output;
        }

        @Override
        public List<ItemStack> getRecipeInputs()
        {
            return recipeItems;
        }
    }
}