package gigaherz.elementsofpower.database.recipes.crafting;

import com.google.common.collect.Lists;
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
        return recipe instanceof ShapelessOreRecipe;
    }

    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe recipe)
    {
        return new RecipeInfo((ShapelessOreRecipe) recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {
        ArrayList<ItemStack> recipeItems = Lists.newArrayList();
        ItemStack output;

        public RecipeInfo(ShapelessOreRecipe recipe)
        {
            output = recipe.getRecipeOutput();

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