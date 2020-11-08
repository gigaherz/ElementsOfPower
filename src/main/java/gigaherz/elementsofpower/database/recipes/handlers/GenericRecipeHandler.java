package gigaherz.elementsofpower.database.recipes.handlers;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.database.recipes.IRecipeHandler;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import gigaherz.elementsofpower.database.recipes.ScaledIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericRecipeHandler implements IRecipeHandler
{
    @Override
    public boolean accepts(@Nonnull IRecipe<?> recipe)
    {
        return !recipe.isDynamic()
                && (recipe.getType() == IRecipeType.SMITHING
                || (recipe.getRecipeOutput().getCount() > 0
                && recipe.getIngredients().stream().anyMatch(i -> i.getMatchingStacks().length > 0)
                && recipe.getIngredients().stream().anyMatch(i -> Arrays.stream(i.getMatchingStacks()).anyMatch(stack -> stack.getCount() > 0))));
    }

    @Override
    public IRecipeInfoProvider handle(@Nonnull IRecipe<?> recipe)
    {
        RecipeInfo<?> info;
        IRecipeType<?> type = recipe.getType();
        if (type == IRecipeType.SMITHING)
            info = new SmithingRecipeInfo((SmithingRecipe)recipe);
        else if (type == IRecipeType.SMELTING ||
                type == IRecipeType.SMOKING ||
                type == IRecipeType.BLASTING)
            info =  new FurnaceRecipeInfo((AbstractCookingRecipe) recipe);
        else
            info =  new RecipeInfo<IRecipe<?>>(recipe);
        info.process();
        return info;
    }

    private static class RecipeInfo<T extends IRecipe<?>> implements IRecipeInfoProvider
    {
        protected T recipe;
        protected ArrayList<ScaledIngredient> inputs = Lists.newArrayList();
        protected ItemStack output;

        public RecipeInfo(T recipe)
        {
            this.recipe = recipe;
            this.output = recipe.getRecipeOutput().copy();
        }

        public void process()
        {
            processIngredients(recipe.getIngredients());
        }

        protected void processIngredients(NonNullList<Ingredient> ingredients)
        {
            for (Ingredient ingredient : ingredients)
            {
                ItemStack[] stacks = ingredient.getMatchingStacks();
                if (stacks.length == 0)
                    continue;

                if(Arrays.stream(stacks).anyMatch(stack -> stack.getCount() > 0))
                {
                    inputs.add(new ScaledIngredient(ingredient,1));
                }
            }
        }

        @Override
        public ItemStack getRecipeOutput()
        {
            return output;
        }

        @Override
        public List<ScaledIngredient> getRecipeInputs()
        {
            return inputs;
        }
    }

    private static class SmithingRecipeInfo extends RecipeInfo<SmithingRecipe>
    {
        public SmithingRecipeInfo(SmithingRecipe recipe)
        {
            super(recipe);
        }

        public void process()
        {
            processIngredients(NonNullList.from(Ingredient.EMPTY, recipe.addition, recipe.base));
        }
    }

    public static class FurnaceRecipeInfo extends RecipeInfo<AbstractCookingRecipe>
    {
        public FurnaceRecipeInfo(AbstractCookingRecipe recipe)
        {
            super(recipe);
        }

        @Override
        public void process()
        {
            super.process();

            int itemsPerCoal = MathHelper.floor(1600.0 / recipe.getCookTime());

            for (int i = 0; i < inputs.size(); i++)
            {
                ScaledIngredient input = inputs.get(i);
                inputs.set(i, input.scale(itemsPerCoal));
            }
            output.setCount(output.getCount() * itemsPerCoal);
        }
    }
}