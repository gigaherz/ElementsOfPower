package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.recipes.handlers.GenericRecipeHandler;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public abstract class RecipeEnumerator
{
    public static List<IRecipeHandler> craftingRecipeHandlers = Lists.newArrayList();

    static
    {
        craftingRecipeHandlers.add(new GenericRecipeHandler());
    }

    abstract void enumerate(MinecraftServer server, @Nonnull IRecipeInfoConsumer consumer);

    public static class Crafting extends RecipeEnumerator
    {
        @Override
        void enumerate(MinecraftServer server, @Nonnull IRecipeInfoConsumer consumer)
        {
            Set<IRecipeSerializer<?>> seenClasses = Sets.newHashSet();

            for (IRecipe<?> recipe : server.getRecipeManager().getRecipes())
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
                    IRecipeSerializer<?> c = recipe.getSerializer();
                    if (!seenClasses.contains(c))
                    {
                        seenClasses.add(c);
                        ElementsOfPowerMod.LOGGER.warn("Ignoring unhandled recipe serializer: " + c.getRegistryName());
                    }
                    continue;
                }

                consumer.process(provider);
            }
        }
    }
}
