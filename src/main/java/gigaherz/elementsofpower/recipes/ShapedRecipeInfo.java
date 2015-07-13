package gigaherz.elementsofpower.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.List;

public class ShapedRecipeInfo implements IRecipeInfoProvider
{

    ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
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
