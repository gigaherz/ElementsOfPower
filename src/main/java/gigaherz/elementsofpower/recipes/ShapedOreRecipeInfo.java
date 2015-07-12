package gigaherz.elementsofpower.recipes;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.List;

public class ShapedOreRecipeInfo implements IRecipeInfoProvider
{

    ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
    ShapedOreRecipe recipe;

    public ShapedOreRecipeInfo(ShapedOreRecipe recipe)
    {
        this.recipe = recipe;

        Object[] input = recipe.getInput();
        for (Object o : input)
        {
            Object oo = o;
            if (oo instanceof List)
            {
                oo = ((List) o).get(0);
            }

            if (oo == null)
                continue;

            if (oo instanceof ItemStack)
            {
                ItemStack c = ((ItemStack) oo).copy();
                c.stackSize = 1;
                recipeItems.add(c);
            }
            else
            {
                ElementsOfPower.logger.warn("Unknown type of item in ShapedOreRecipe: " + o.getClass().getName());
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
