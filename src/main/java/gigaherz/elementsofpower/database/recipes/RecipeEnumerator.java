package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.recipes.crafting.ShapedOreRecipeHandler;
import gigaherz.elementsofpower.database.recipes.crafting.ShapedRecipeHandler;
import gigaherz.elementsofpower.database.recipes.crafting.ShapelessOreRecipeHandler;
import gigaherz.elementsofpower.database.recipes.crafting.ShapelessRecipeHandler;
import gigaherz.elementsofpower.database.recipes.furnace.FurnaceRecipeInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RecipeEnumerator
{
    public static List<IRecipeHandler> craftingRecipeHandlers = Lists.newArrayList();

    static
    {
        craftingRecipeHandlers.add(new ShapedRecipeHandler());
        craftingRecipeHandlers.add(new ShapelessRecipeHandler());
        craftingRecipeHandlers.add(new ShapedOreRecipeHandler());
        craftingRecipeHandlers.add(new ShapelessOreRecipeHandler());
    }

    abstract void enumerate(@Nonnull IRecipeInfoConsumer consumer);

    public static class Crafting extends RecipeEnumerator
    {
        @Override
        void enumerate(@Nonnull IRecipeInfoConsumer consumer)
        {
            Set<Class<? extends IRecipe>> seenClasses = Sets.newHashSet();

            for (IRecipe recipe : CraftingManager.getInstance().getRecipeList())
            {
                ItemStack recipeOutput = recipe.getRecipeOutput();
                if (recipeOutput == null || recipeOutput.getItem() == null)
                    continue;

                IRecipeInfoProvider provider = null;

                for (IRecipeHandler h : craftingRecipeHandlers)
                {
                    if (h.accepts(recipe))
                    {
                        provider = h.handle(recipe);
                        break;
                    }
                }

                if (provider == null)
                {
                    Class<? extends IRecipe> c = recipe.getClass();
                    if (!seenClasses.contains(c))
                    {
                        seenClasses.add(c);
                        ElementsOfPower.logger.warn("Ignoring unhandled recipe class: " + c.getName());
                    }
                    continue;
                }

                consumer.process(provider);
            }
        }
    }

    public static class Furnace extends RecipeEnumerator
    {
        @Override
        void enumerate(@Nonnull IRecipeInfoConsumer consumer)
        {
            for (Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet())
            {
                ItemStack output = entry.getValue();
                if (output == null || output.getItem() == null)
                    continue;

                ItemStack input = entry.getKey();
                if (input == null || input.getItem() == null)
                    continue;

                consumer.process(new FurnaceRecipeInfo(input, output));
            }
        }
    }
}
