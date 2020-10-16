package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.ConversionCache;
import gigaherz.elementsofpower.database.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public class RecipeTools
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<RecipeEnumerator> recipeEnumerators = Lists.newArrayList();

    static
    {
        recipeEnumerators.add(new RecipeEnumerator.Crafting());
    }

    public static Map<Item, ItemSource> gatherRecipes(ServerWorld world)
    {
        Processor p = new Processor(world);
        for (RecipeEnumerator re : recipeEnumerators)
        {
            re.enumerate(p::processRecipe);
        }
        return p.itemSources;
    }

    public static class ItemSource
    {
        public final Item item;
        public int numProduced;
        public final Set<Item> allIntermediates = new HashSet<>();
        public final List<ItemStack> sources = new ArrayList<>();

        public ItemSource(Item item, int count, Set<Item> allIntermediates)
        {
            this.item = item;
            this.numProduced = count;
            this.allIntermediates.addAll(allIntermediates);
        }

        private boolean isRecipeAggregating()
        {
            return sources.size() == 1
                    && sources.get(0).getCount() < numProduced;
        }

        private void addCompacting(@Nonnull ItemStack input)
        {
            boolean found = false;

            for (ItemStack k : sources)
            {
                if (input.getItem() == k.getItem())
                {
                    if (k != input)
                    {
                        k.grow(input.getCount());
                    }
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                sources.add(input.copy());
            }
        }

        @Override
        public String toString()
        {
            return String.format("{ItemSource: %s}", item);
        }
    }

    private static class Processor
    {
        public final ServerWorld world;
        public Map<Item, ItemSource> itemSources = Maps.newHashMap();

        private Processor(ServerWorld world)
        {
            this.world = world;
        }

        private void processRecipe(@Nonnull IRecipeInfoProvider recipe)
        {
            ItemStack output = recipe.getRecipeOutput();

            if (output.getCount() == 0)
            {
                ElementsOfPowerMod.LOGGER.debug("Recipe with output '" + output + "' has stack size 0. This recipe will be ignored.");
                return;
            }

            Item item = output.getItem();
            if (itemSources.containsKey(item))
            {
                return;
            }

            if (ConversionCache.get(world).hasEssences(item))
            {
                ElementsOfPowerMod.LOGGER.debug("Recipe with output '" + output + "' results in item with explicitly-set values. This recipe will be ignored.");
                return;
            }

            for (ItemStack s : recipe.getRecipeInputs())
            {
                if (s != null)
                {
                    if (s.getItem() == item)
                    {
                        ElementsOfPowerMod.LOGGER.debug("Recipe with output '" + output + "' uses itself as an input. This recipe will be ignored.");
                        return;
                    }
                }
            }

            ItemSource source1 = reduceItemsList(item, recipe.getRecipeInputs(), output.getCount());
            ItemSource source2 = applyExistingRecipes(item, source1);

            replaceExistingSources(item, source2);

            itemSources.put(item, source2);
        }

        @Nonnull
        public ItemSource applyExistingRecipes(Item item, @Nonnull ItemSource source)
        {
            return replaceSources(item, source, itemSources);
        }

        private void replaceExistingSources(Item item, @Nonnull ItemSource source)
        {
            Map<Item, ItemSource> itemsToReplace = Maps.newHashMap();
            Map<Item, ItemSource> singletonMap = Collections.singletonMap(item, source);

            for (Map.Entry<Item, ItemSource> entry : itemSources.entrySet())
            {
                Item result = entry.getKey();
                ItemSource oldSource = entry.getValue();
                ItemSource newSource = replaceSources(item, oldSource, singletonMap);
                if (newSource != oldSource)
                {
                    itemsToReplace.put(result, newSource);
                }
            }

            itemSources.putAll(itemsToReplace);
        }

        @Nonnull
        public ItemSource replaceSources(Item item0, @Nonnull ItemSource source, Map<Item, ItemSource> sources)
        {
            ItemSource result = new ItemSource(item0, source.numProduced, source.allIntermediates);

            int totalMult = 1;
            boolean anythingChanged = false;

            // Replace each input by the contents of its sources, if available.
            for (ItemStack is : source.sources)
            {
                Item item = is.getItem();
                ItemSource sourceData = sources.get(item);

                boolean good = false;
                if (sourceData != null)
                {
                    int numNeeded = is.getCount();
                    int numProduced = sourceData.numProduced;
                    int num = Utils.lcm(numNeeded, numProduced);

                    int mult = num / numNeeded;

                    totalMult *= mult;
                    result.numProduced *= mult;

                    good = true;
                    for (ItemStack t : sourceData.sources)
                    {
                        if (source.allIntermediates.contains(t.getItem()))
                        {
                            good = false;
                            break;
                        }
                    }

                    if(numProduced == 0)
                    {
                        LOGGER.error("Source has numProduced=0!!! {}", source);
                        good=false;
                    }

                    if (good)
                    {
                        for (ItemStack t : result.sources)
                        {
                            t.setCount(t.getCount() * mult);
                        }

                        int mult2 = num / numProduced;
                        for (ItemStack t : sourceData.sources)
                        {
                            ItemStack q = t.copy();
                            q.setCount(q.getCount() * mult2);
                            result.addCompacting(q);
                        }

                        anythingChanged = true;
                    }
                }

                if (!good)
                {
                    ItemStack q = is.copy();
                    q.setCount(q.getCount() * totalMult);
                    result.addCompacting(q);
                }
            }

            if (anythingChanged)
            {
                // Simplify
                if (result.numProduced > 1)
                {
                    int cd = result.numProduced;
                    for (ItemStack is : result.sources)
                    {
                        cd = Utils.gcd(cd, is.getCount());
                    }

                    if (cd > 1)
                    {
                        for (ItemStack is : result.sources)
                        {
                            is.setCount(is.getCount() / cd);
                        }
                        result.numProduced /= cd;
                    }
                }

                return result;
            }

            return source;
        }

        @Nonnull
        public ItemSource reduceItemsList(Item item, @Nonnull List<ItemStack> items, int count)
        {
            ItemSource source = new ItemSource(item, count, Collections.singleton(item));

            for (ItemStack is : items)
            {
                if (is.isEmpty())
                    continue;

                source.addCompacting(is);
            }

            return source;
        }
    }
}