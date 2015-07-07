package gigaherz.elementsofpower.recipes;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShapedRecipeInfo implements IRecipeInfoProvider {

    ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
    ShapedRecipes recipe;

    public ShapedRecipeInfo(ShapedRecipes recipe)
    {
        this.recipe = recipe;

        ItemStack[] input = recipe.recipeItems;
        for(ItemStack o : input)
        {
            if(o == null)
                continue;

            ItemStack c = o.copy();
            c.stackSize = 1;
            recipeItems.add(c);
        }
    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipe.getRecipeOutput();
    }

    @Override
    public List<ItemStack> getRecipeInputs() {
        return recipeItems;
    }
}
