package gigaherz.elementsofpower.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.ArrayList;
import java.util.List;

public class ShapelessRecipeInfo implements IRecipeInfoProvider
{

    ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
    ShapelessRecipes recipe;

    public ShapelessRecipeInfo(ShapelessRecipes recipe)
    {
        this.recipe = recipe;

        List<ItemStack> input = recipe.recipeItems;
        for (ItemStack o : input)
        {
            if (o == null)
                continue;

            ItemStack c = o.copy();
            c.stackSize = 1;
            recipeItems.add(c);
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
