package gigaherz.elementsofpower.database.recipes.crafting;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.database.recipes.IRecipeHandler;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenericRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(@Nonnull IRecipe recipe)
    {
        return !recipe.isDynamic();
    }

    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe recipe)
    {
        return new RecipeInfo(recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {
        ArrayList<ItemStack> recipeItems = Lists.newArrayList();
        ItemStack output;

        public RecipeInfo(IRecipe recipe)
        {
            output = recipe.getRecipeOutput();

            NonNullList<Ingredient> inputs = recipe.getIngredients();
            for (Ingredient input : inputs)
            {
                ItemStack actualInput = input.getMatchingStacks()[0];

                ItemStack stack = (actualInput).copy();
                stack.setCount(1);
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