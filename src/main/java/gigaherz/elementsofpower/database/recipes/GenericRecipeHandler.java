package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenericRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(@Nonnull IRecipe<?> recipe)
    {
        return !recipe.isDynamic() && recipe.getIngredients().stream().allMatch(i -> i.getMatchingStacks().length > 0);
    }

    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe<?> recipe)
    {
        IRecipeType<?> type = recipe.getType();
        if (type == IRecipeType.SMELTING ||
                type == IRecipeType.SMOKING ||
                type == IRecipeType.BLASTING)
            return new FurnaceRecipeInfo((AbstractCookingRecipe) recipe);
        return new RecipeInfo(recipe);
    }

    private static class RecipeInfo implements IRecipeInfoProvider
    {
        protected ArrayList<ItemStack> inputs = Lists.newArrayList();
        protected ItemStack output;

        public RecipeInfo(IRecipe<?> recipe)
        {
            output = recipe.getRecipeOutput().copy();

            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (Ingredient ingredient : ingredients)
            {
                ItemStack[] stacks = ingredient.getMatchingStacks();
                if (stacks.length == 0)
                    continue;

                ItemStack actualInput = stacks[0];

                ItemStack stack = actualInput.copy();
                stack.setCount(1);
                inputs.add(stack);
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
            return inputs;
        }
    }

    public static class FurnaceRecipeInfo extends RecipeInfo
    {
        public FurnaceRecipeInfo(AbstractCookingRecipe recipe)
        {
            super(recipe);

            int itemsPerCoal = MathHelper.floor(1600.0 / recipe.getCookTime());

            for (ItemStack input : inputs)
            { input.setCount(input.getCount() * itemsPerCoal); }
            output.setCount(output.getCount() * itemsPerCoal);
        }
    }
}