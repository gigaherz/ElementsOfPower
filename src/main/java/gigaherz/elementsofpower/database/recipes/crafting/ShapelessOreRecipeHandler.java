package gigaherz.elementsofpower.database.recipes.crafting;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.recipes.IRecipeHandler;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShapelessOreRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(@Nonnull IRecipe recipe)
    {
        return recipe.getRecipeOutput() != null && recipe instanceof ShapelessOreRecipe;
    }

    @Nonnull
    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe recipe)
    {
        return new RecipeInfo((ShapelessOreRecipe) recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {
        ArrayList<ItemStack> recipeItems = new ArrayList<>();
        ShapelessOreRecipe recipe;

        public RecipeInfo(ShapelessOreRecipe recipe)
        {
            this.recipe = recipe;

            ArrayList<Object> inputs = recipe.getInput();
            for (Object input : inputs)
            {
                Object actualInput = input;
                if (actualInput instanceof List)
                {
                    if (((List) actualInput).size() > 0)
                        actualInput = ((List) input).get(0);
                }

                if (actualInput == null)
                    continue;

                if (actualInput instanceof ItemStack)
                {
                    ItemStack stack = ((ItemStack) actualInput).copy();
                    stack.stackSize = 1;
                    recipeItems.add(stack);
                }
                else
                {
                    ElementsOfPower.logger.warn("Unknown type of item in ShapedOreRecipe: " + input.getClass().getName());
                }
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