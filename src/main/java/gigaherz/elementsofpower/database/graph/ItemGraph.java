package gigaherz.elementsofpower.database.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class ItemGraph<T>
{
    private final Map<Item, ItemNode<T>> itemNodes = Maps.newHashMap();
    private final List<RecipeNode<T>> recipeNodes = Lists.newArrayList();

    public Stream<ItemNode<T>> getItemNodes()
    {
        return itemNodes.values().stream();
    }

    public Stream<RecipeNode<T>> getRecipeNodes()
    {
        return recipeNodes.stream();
    }

    public void addEdgeBundle(Item consumer, double scale, Iterable<Map.Entry<Item, Double>> to, Object recipe)
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

    public interface ScaleFunction<T>
    {
        T scale(T original, double scale);
    }

    public void floodFill(Item key, T fillWith, ScaleFunction<T> multiply, BiFunction<T, T, T> adder, BiPredicate<T, T> replace)
    {
        ItemNode<T> start = itemNodes.get(key);
        if (start == null)
            return;

        Queue<ItemNode<T>> dirtyItems = new ArrayDeque<>();
        Queue<RecipeNode<T>> dirtyRecipes = new ArrayDeque<>();

        start.providedValue = new ItemValue<>(start, fillWith);
        dirtyItems.add(start);

        while(dirtyItems.size() > 0 || dirtyRecipes.size() > 0)
        {
            while(dirtyRecipes.size() > 0)
            {
                RecipeNode<T> current = dirtyRecipes.remove();
                // if recipe is satisfied:
                //     mark item nodes as dirty
            }

            while(dirtyItems.size() > 0)
            {
                ItemNode<T> current = dirtyItems.remove();

            }
        }
    }

    public void finishFill(BiConsumer<Item, T> consumer)
    {
        itemNodes.values().forEach(value -> {
            // todo: return calculated values
            if (value.providedValue != null)
            {
                consumer.accept(value.owner, value.providedValue.value);
            }
            value.providedValue = null;
        });
        recipeNodes.forEach(value -> {
            value.providedValue = null;
        });
    }

    public static class ItemNode<T>
    {
        public final Item owner;
        public final Set<ConsumerEdge<T>> providers = Sets.newHashSet();
        public final Set<ProviderEdge<T>> consumers = Sets.newHashSet();

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
        public final Set<ConsumerEdge<T>> consumers = Sets.newHashSet();
        public final Set<ProviderEdge<T>> providers = Sets.newHashSet();

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
        public final Set<RecipeValue<T>> via = Sets.newHashSet();
        public T value;

        public ItemValue(ItemNode<T> owner, T value)
        {
            this.owner = owner;
            this.value = value;
        }

        public boolean contains(ItemNode<T> node)
        {
            if (owner == node)
                return true;
            for(RecipeValue<T> v : via)
            {
                if (v.contains(node))
                    return true;
            }
            return false;
        }

        @Override
        public String toString()
        {
            return String.format("{Value: %s = %s via %s}", owner, value, via);
        }
    }

    public static class RecipeValue<T>
    {
        public final RecipeNode<T> owner;
        public final Set<ItemValue<T>> via = Sets.newHashSet();
        public T value;

        public RecipeValue(RecipeNode<T> owner, T value)
        {
            this.owner = owner;
            this.value = value;
        }

        public boolean contains(ItemNode<T> node)
        {
            for(ItemValue<T> v : via)
            {
                if (v.contains(node))
                    return true;
            }
            return false;
        }

        @Override
        public String toString()
        {
            return String.format("{Value: %s = %s via %s}", owner, value, via);
        }
    }
}
