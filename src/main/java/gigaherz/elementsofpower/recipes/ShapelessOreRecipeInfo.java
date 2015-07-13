package gigaherz.elementsofpower.recipes;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

public class ShapelessOreRecipeInfo implements IRecipeInfoProvider
{

    ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
    ShapelessOreRecipe recipe;

    public ShapelessOreRecipeInfo(ShapelessOreRecipe recipe)
    {
        this.recipe = recipe;

        ArrayList<Object> inputs = recipe.getInput();
        for (Object input : inputs)
        {
            Object actualInput = input;
            if (actualInput instanceof List)
            {
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
        return recipe.getRecipeOutput();
    }

    @Override
    public List<ItemStack> getRecipeInputs()
    {
        return recipeItems;
    }
}
