package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.recipes.crafting.GenericRecipeHandler;
import gigaherz.elementsofpower.database.recipes.furnace.FurnaceRecipeInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RecipeEnumerator
{
    public static List<IRecipeHandler> craftingRecipeHandlers = Lists.newArrayList();

    static
    {
        craftingRecipeHandlers.add(new GenericRecipeHandler());
    }

    abstract void enumerate(@Nonnull IRecipeInfoConsumer consumer);

    public static class Crafting extends RecipeEnumerator
    {
        @Override
        void enumerate(@Nonnull IRecipeInfoConsumer consumer)
        {
            Set<Class<? extends IRecipe>> seenClasses = Sets.newHashSet();

            for (IRecipe recipe : ForgeRegistries.RECIPES)
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
                        ElementsOfPowerMod.logger.warn("Ignoring unhandled recipe class: " + c.getName());
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
                ItemStack input = entry.getKey();
                consumer.process(new FurnaceRecipeInfo(input, output));
            }
        }
    }
}
