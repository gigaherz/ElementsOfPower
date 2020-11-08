package gigaherz.elementsofpower.database.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gigaherz.elementsofpower.ConfigManager;
import gigaherz.elementsofpower.database.recipes.ScaledIngredient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ItemGraph<T>
{
    public static final Logger LOGGER = LogManager.getLogger();

    public interface ScaleFunction<T>
    {
        T scale(T original, double scale);
    }

    public interface AddFunction<T>
    {
        T add(T original, T scale);
    }

    public interface MinFunction<T>
    {
        T min(T original, T scale);
    }

    private final Map<Item, ItemNode<T>> itemNodes = Maps.newHashMap();
    private final Map<Ingredient, IngredientNode<T>> ingredientNodes = Maps.newHashMap();
    private final List<RecipeNode<T>> recipeNodes = Lists.newArrayList();
    private final ScaleFunction<T> scaler;
    private final AddFunction<T> adder;
    private final Comparator<T> comparator;
    private final MinFunction<T> lesser;
    private final Predicate<T> isNullOrEmpty;
    @Nonnull
    private final T defaultValue;

    public ItemGraph(ScaleFunction<T> scaler, AddFunction<T> adder, Comparator<T> comparator, MinFunction<T> lesser, Predicate<T> isNullOrEmpty, T defaultValue)
    {
        this.scaler = scaler;
        this.adder = adder;
        this.comparator = comparator;
        this.lesser = lesser;
        this.isNullOrEmpty = isNullOrEmpty;
        this.defaultValue = defaultValue;
    }

    public Stream<ItemNode<T>> getItemNodes()
    {
        return itemNodes.values().stream();
    }

    public Stream<RecipeNode<T>> getRecipeNodes()
    {
        return recipeNodes.stream();
    }

    public void addRecipe(ItemStack stack, Iterable<ScaledIngredient> to, Object recipe)
    {
        Item consumer = stack.getItem();
        ItemNode<T> consumerNode = getOrCreateItemNode(consumer);
        RecipeNode<T> recipeNode = getOrCreateRecipeNode(recipe);
        attachConsumer(consumerNode, recipeNode);
        for (ScaledIngredient cs : to)
        {
            IngredientNode<T> providerNode = getOrAddIngredient(cs.ingredient);
            double edgeWeight = cs.scale / stack.getCount();
            attachIngredient(recipeNode, providerNode, edgeWeight);
        }
    }

    private IngredientNode<T> getOrAddIngredient(Ingredient key)
    {
        return ingredientNodes.computeIfAbsent(key, ingredient -> {
            IngredientNode<T> node = new IngredientNode<T>(key);
            for (ItemStack stack : ingredient.getMatchingStacks())
            {
                if (stack.getCount() > 0)
                {
                    ItemNode<T> itemNode = getOrCreateItemNode(stack.getItem());
                    attachProvider(node, itemNode, 1.0 / stack.getCount());
                }
            }
            return node;
        });
    }

    private void attachConsumer(ItemNode<T> consumerNode, RecipeNode<T> recipeNode)
    {
        ConsumerEdge<T> consumerEdge = new ConsumerEdge<>(consumerNode, recipeNode);
        consumerNode.providers.add(consumerEdge);
        recipeNode.consumers.add(consumerEdge);
    }

    private void attachIngredient(RecipeNode<T> recipeNode, IngredientNode<T> providerNode, double edgeWeight)
    {
        IngredientEdge<T> consumerEdge = new IngredientEdge<>(recipeNode, providerNode, edgeWeight);
        recipeNode.ingredients.add(consumerEdge);
        providerNode.consumers.add(consumerEdge);
    }

    private void attachProvider(IngredientNode<T> ingredientNode, ItemNode<T> providerNode, double edgeWeight)
    {
        ProviderEdge<T> consumerEdge = new ProviderEdge<>(ingredientNode, providerNode, edgeWeight);
        ingredientNode.providers.add(consumerEdge);
        providerNode.consumers.add(consumerEdge);
    }

    private ItemNode<T> getOrCreateItemNode(Item from)
    {
        return itemNodes.computeIfAbsent(from, ItemNode::new);
    }

    private RecipeNode<T> getOrCreateRecipeNode(Object from)
    {
        RecipeNode<T> node = new RecipeNode<>(from);
        recipeNodes.add(node);
        return node;
    }

    public void addData(Item key, T value)
    {
        ItemNode<T> node = itemNodes.get(key);
        if (node == null)
            return;

        node.providedValue = value;
    }

    public void spread()
    {
        Queue<ItemNode<T>> dirtyItems = new ArrayDeque<>();
        Queue<RecipeNode<T>> dirtyRecipes = new ArrayDeque<>();
        Queue<IngredientNode<T>> dirtyIngredients = new ArrayDeque<>(ingredientNodes.values());

        //noinspection ConstantConditions
        while (dirtyItems.size() > 0 || dirtyIngredients.size() > 0 || dirtyRecipes.size() > 0)
        {
            while (dirtyIngredients.size() > 0)
            {
                IngredientNode<T> current = dirtyIngredients.remove();
                if (current.providedValue == null && current.providers.size() == 1 && current.providers.stream().anyMatch(provider -> provider.provider.providedValue != null))
                {
                    T acc = null;
                    for (ProviderEdge<T> provider : current.providers)
                    {
                        T value = scaler.scale(provider.provider.providedValue, provider.edgeWeight);
                        acc = acc == null ? value : adder.add(acc, value);
                    }
                    current.providedValue = acc;
                    current.consumers.forEach(consumer -> dirtyRecipes.add(consumer.consumer));
                }
            }

            while (dirtyRecipes.size() > 0)
            {
                RecipeNode<T> current = dirtyRecipes.remove();
                if (current.providedValue == null && current.ingredients.stream().allMatch(provider -> provider.provider.providedValue != null))
                {
                    T acc = null;
                    for (IngredientEdge<T> provider : current.ingredients)
                    {
                        T value = scaler.scale(provider.provider.providedValue, provider.edgeWeight);
                        acc = acc == null ? value : adder.add(acc, value);
                    }
                    current.providedValue = acc;
                    current.consumers.forEach(consumer -> dirtyItems.add(consumer.consumer));
                }
            }

            while (dirtyItems.size() > 0)
            {
                ItemNode<T> current = dirtyItems.remove();
                if (current.providedValue == null && current.providers.size() == 1 && current.providers.stream().anyMatch(provider -> provider.provider.providedValue != null))
                {
                    Optional<T> min = current.providers.stream()
                            .filter(provider -> provider.provider.providedValue != null)
                            .map(provider -> provider.provider.providedValue)
                            .min(comparator);
                    min.ifPresent(value -> {
                        current.providedValue = value;
                        current.consumers.forEach(consumer -> dirtyIngredients.add(consumer.consumer));
                    });
                }
            }
        }
    }

    public void computeFinalValues(BiConsumer<Item, T> consumer)
    {
        List<ItemNode<T>> values = new ArrayList<>(itemNodes.values());

        for (int i = 0; i < values.size(); i++)
        {
            ItemNode<T> tItemNode = values.get(i);

            if (ConfigManager.COMMON.verboseDebug.get())
                LOGGER.debug("Processing {}...", tItemNode.owner);

            T value = calculateValue(tItemNode);
            if (value != null)
            {
                consumer.accept(tItemNode.owner, value);
            }

            if (ConfigManager.COMMON.verboseDebug.get())
                LOGGER.debug("Result {} / {} items left...", value, values.size() - i - 1);
        }
        itemNodes.values().forEach(node -> {
            node.providedValue = null;
        });
        recipeNodes.forEach(value -> {
            value.providedValue = null;
        });
    }

    private T calculateValue(ItemNode<T> node)
    {
        if (node.providedValue != null)
            return isNullOrEmpty.test(node.providedValue) ? null : node.providedValue;

        Stack<Object> visitedNodes = new Stack<>();
        return calculateRecursive(node, visitedNodes);
    }

    private int maxDepth = 0;

    private T calculateRecursive(ItemNode<T> node, Stack<Object> visitedNodes)
    {
        if (visitedNodes.contains(node))
            return null;
        visitedNodes.push(node);
        maxDepth = Math.max(maxDepth, visitedNodes.size());
        try
        {
            T value = node.providedValue;
            if (value == null)
            {
                for (ConsumerEdge<T> provider : node.providers)
                {
                    T recipeValue = calculateRecursive(provider.provider, visitedNodes);
                    if (recipeValue != null)
                    {
                        value = value != null ? lesser.min(value, recipeValue) : recipeValue;
                    }
                }
                node.providedValue = value != null ? value : defaultValue;
            }
            return isNullOrEmpty.test(value) ? null : value;
        }
        finally
        {
            visitedNodes.pop();
        }
    }

    private T calculateRecursive(RecipeNode<T> node, Stack<Object> visitedNodes)
    {
        if (visitedNodes.contains(node))
            return null;
        visitedNodes.push(node);
        maxDepth = Math.max(maxDepth, visitedNodes.size());
        try
        {
            T value = node.providedValue;
            if (value == null)
            {
                boolean anyMissing = false;
                for (IngredientEdge<T> provider : node.ingredients)
                {
                    T recipeValue = calculateRecursive(provider.provider, visitedNodes);
                    if (recipeValue != null)
                    {
                        recipeValue = scaler.scale(recipeValue, provider.edgeWeight);
                        value = value != null ? adder.add(value, recipeValue) : recipeValue;
                    }
                    else
                    {
                        anyMissing = true;
                        break;
                    }
                }
                if (anyMissing)
                    value = null;
                //node.providedValue = value != null ? value : defaultValue;
            }
            return isNullOrEmpty.test(value) ? null : value;
        }
        finally
        {
            visitedNodes.pop();
        }
    }

    private T calculateRecursive(IngredientNode<T> node, Stack<Object> visitedNodes)
    {
        if (visitedNodes.contains(node))
            return null;
        visitedNodes.push(node);
        maxDepth = Math.max(maxDepth, visitedNodes.size());
        try
        {
            T value = node.providedValue;
            if (value == null)
            {
                for (ProviderEdge<T> provider : node.providers)
                {
                    T recipeValue = calculateRecursive(provider.provider, visitedNodes);
                    if (recipeValue != null)
                    {
                        recipeValue = scaler.scale(recipeValue, provider.edgeWeight);
                        value = value != null ? lesser.min(value, recipeValue) : recipeValue;
                    }
                }
                //node.providedValue = value != null ? value : defaultValue;
            }
            return isNullOrEmpty.test(value) ? null : value;
        }
        finally
        {
            visitedNodes.pop();
        }
    }

    public static class ItemNode<T>
    {
        public final Item owner;
        public final LinkedHashSet<ConsumerEdge<T>> providers = Sets.newLinkedHashSet();
        public final LinkedHashSet<ProviderEdge<T>> consumers = Sets.newLinkedHashSet();

        public T providedValue;

        public ItemNode(Item owner)
        {
            this.owner = owner;
        }

        @Override
        public String toString()
        {
            return String.format("{Item: %s}", owner);
        }
    }

    public static class RecipeNode<T>
    {
        public final Object owner;
        public final LinkedHashSet<ConsumerEdge<T>> consumers = Sets.newLinkedHashSet();
        public final LinkedHashSet<IngredientEdge<T>> ingredients = Sets.newLinkedHashSet();

        public T providedValue;

        public RecipeNode(Object owner)
        {
            this.owner = owner;
        }

        @Override
        public String toString()
        {
            return String.format("{Recipe: %s}", owner);
        }
    }

    public static class IngredientNode<T>
    {
        public final Ingredient owner;
        public final LinkedHashSet<IngredientEdge<T>> consumers = Sets.newLinkedHashSet();
        public final LinkedHashSet<ProviderEdge<T>> providers = Sets.newLinkedHashSet();

        public T providedValue;

        public IngredientNode(Ingredient owner)
        {
            this.owner = owner;
        }

        @Override
        public String toString()
        {
            return String.format("{Ingredient: %s}", CraftingHelper.getID(owner.getSerializer()));
        }
    }

    public static class ConsumerEdge<T>
    {
        public final ItemNode<T> consumer;
        public final RecipeNode<T> provider;

        public ConsumerEdge(ItemNode<T> consumer, RecipeNode<T> provider)
        {
            this.consumer = consumer;
            this.provider = provider;
        }

        @Override
        public String toString()
        {
            return String.format("{Edge: %s <- %s}", consumer, provider);
        }
    }

    public static class IngredientEdge<T>
    {
        public final RecipeNode<T> consumer;
        public final IngredientNode<T> provider;
        public final double edgeWeight;

        public IngredientEdge(RecipeNode<T> consumer, IngredientNode<T> provider, double edgeWeight)
        {
            this.consumer = consumer;
            this.provider = provider;
            this.edgeWeight = edgeWeight;
        }

        @Override
        public String toString()
        {
            return String.format("{Edge: %s <- %s}", consumer, provider);
        }
    }

    public static class ProviderEdge<T>
    {
        public final IngredientNode<T> consumer;
        public final ItemNode<T> provider;
        public final double edgeWeight;

        public ProviderEdge(IngredientNode<T> consumer, ItemNode<T> provider, double edgeWeight)
        {
            this.consumer = consumer;
            this.provider = provider;
            this.edgeWeight = edgeWeight;
        }

        @Override
        public String toString()
        {
            return String.format("{Edge: %s <- %s}", consumer, provider);
        }
    }
}
