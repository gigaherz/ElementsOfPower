package gigaherz.elementsofpower.database.recipes;

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
import java.util.*;

public abstract class RecipeEnumerator
{
    public static List<IRecipeHandler> craftingRecipeHandlers = new ArrayList<>();

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
            Set<Class<? extends IRecipe>> seenClasses = new HashSet<>();

            for (IRecipe recipe : CraftingManager.getInstance().getRecipeList())
            {
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

                if(entry.getKey() == null || entry.getKey().getItem() == null)
                    continue;

                consumer.process(new FurnaceRecipeInfo(entry.getKey(), entry.getValue()));
            }
        }
    }
}
