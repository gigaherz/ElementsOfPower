package gigaherz.elementsofpower.database.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class ItemGraph<T>
{
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
    private final List<RecipeNode<T>> recipeNodes = Lists.newArrayList();
    private final ScaleFunction<T> scaler;
    private final AddFunction<T> adder;
    private final Comparator<T> comparator;
    private final MinFunction<T> lesser;

    public ItemGraph(ScaleFunction<T> scaler, AddFunction<T> adder, Comparator<T> comparator, MinFunction<T> lesser)
    {
        this.scaler = scaler;
        this.adder = adder;
        this.comparator = comparator;
        this.lesser = lesser;
    }

    public Stream<ItemNode<T>> getItemNodes()
    {
        return itemNodes.values().stream();
    }

    public Stream<RecipeNode<T>> getRecipeNodes()
    {
        return recipeNodes.stream();
    }

    public void addRecipe(Item consumer, double scale, Iterable<Map.Entry<Item, Double>> to, Object recipe)
    {
        ItemNode<T> consumerNode = getOrCreateItemNode(consumer);
        RecipeNode<T> recipeNode = getOrCreateRecipeNode(recipe);
        attachConsumer(consumerNode, recipeNode);
        for(Map.Entry<Item, Double> cs : to)
        {
            ItemNode<T> providerNode = getOrCreateItemNode(cs.getKey());
            double edgeWeight = cs.getValue()/scale;
            attachProvider(providerNode, recipeNode, edgeWeight);
        }
    }

    private void attachConsumer(ItemNode<T> consumerNode, RecipeNode<T> recipeNode)
    {
        ConsumerEdge<T> consumerEdge = new ConsumerEdge<>(consumerNode, recipeNode);
        consumerNode.providers.add(consumerEdge);
        recipeNode.consumers.add(consumerEdge);
    }

    private void attachProvider(ItemNode<T> providerNode, RecipeNode<T> recipeNode, double edgeWeight)
    {
        ProviderEdge<T> consumerEdge = new ProviderEdge<>(recipeNode, providerNode, edgeWeight);
        recipeNode.providers.add(consumerEdge);
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

    public void addData(Item key, T fillWith)
    {
        ItemNode<T> node = itemNodes.get(key);
        if (node == null)
            return;

        node.providedValue = new ItemValue<>(node, fillWith);
    }

    public void spread()
    {
        Queue<ItemNode<T>> dirtyItems = new ArrayDeque<>();
        Queue<RecipeNode<T>> dirtyRecipes = new ArrayDeque<>(recipeNodes);

        //noinspection ConstantConditions
        while(dirtyItems.size() > 0 || dirtyRecipes.size() > 0)
        {
            while(dirtyRecipes.size() > 0)
            {
                RecipeNode<T> current = dirtyRecipes.remove();
                if (current.providedValue == null && current.providers.stream().allMatch(provider -> provider.provider.providedValue != null))
                {
                    T acc = null;
                    for(ProviderEdge<T> provider : current.providers)
                    {
                        T value = scaler.scale(provider.provider.providedValue.value, provider.edgeWeight);
                        acc = acc == null ? value : adder.add(acc, provider.provider.providedValue.value);
                    }
                    current.providedValue = new RecipeValue<>(current, acc);
                    current.consumers.forEach(consumer -> dirtyItems.add(consumer.consumer));
                }
            }

            while(dirtyItems.size() > 0)
            {
                ItemNode<T> current = dirtyItems.remove();
                if (current.providedValue == null && current.providers.size() == 1 && current.providers.stream().anyMatch(provider -> provider.provider.providedValue != null))
                {
                    Optional<T> min = current.providers.stream()
                            .filter(provider -> provider.provider.providedValue != null)
                            .map(provider -> provider.provider.providedValue.value)
                            .min(comparator);
                    min.ifPresent(value -> {
                        current.providedValue = new ItemValue<>(current, value);
                        current.consumers.forEach(consumer -> dirtyRecipes.add(consumer.consumer));
                    });
                }
            }
        }
    }

    public void finishFill(BiConsumer<Item, T> consumer)
    {
        itemNodes.values().forEach(node -> {
            T value = calculateValue(node);
            if (value != null)
            {
                consumer.accept(node.owner, value);
            }
            node.providedValue = null;
        });
        recipeNodes.forEach(value -> {
            value.providedValue = null;
        });
    }

    private T calculateValue(ItemNode<T> node)
    {
        if (node.providedValue != null)
            return node.providedValue.value;

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
            T value = node.providedValue != null ? node.providedValue.value : null;
            if (value == null)
            {
                for(ConsumerEdge<T> provider : node.providers)
                {
                    T recipeValue = calculateRecursive(provider.provider, visitedNodes);
                    if (recipeValue != null)
                    {
                        value = value != null ? lesser.min(value, recipeValue) : recipeValue;
                    }
                }
            }
            return value;
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
            T value = node.providedValue != null ? node.providedValue.value : null;
            if (value == null)
            {
                boolean anyMissing = false;
                for(ProviderEdge<T> provider : node.providers)
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
                {
                    return null;
                }
            }
            return value;
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

        public ItemValue<T> providedValue;

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
        public final LinkedHashSet<ProviderEdge<T>> providers = Sets.newLinkedHashSet();

        public RecipeValue<T> providedValue;

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

    public static class ProviderEdge<T>
    {
        public final RecipeNode<T> consumer;
        public final ItemNode<T> provider;
        public final double edgeWeight;

        public ProviderEdge(RecipeNode<T> consumer, ItemNode<T> provider, double edgeWeight)
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

    public static class ItemValue<T>
    {
        public final ItemNode<T> owner;
        public T value;

        public ItemValue(ItemNode<T> owner, T value)
        {
            this.owner = owner;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return String.format("{Value: %s = %s}", owner, value);
        }
    }

    public static class RecipeValue<T>
    {
        public final RecipeNode<T> owner;
        public T value;

        public RecipeValue(RecipeNode<T> owner, T value)
        {
            this.owner = owner;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return String.format("{Value: %s = %s}", owner, value);
        }
    }
}
