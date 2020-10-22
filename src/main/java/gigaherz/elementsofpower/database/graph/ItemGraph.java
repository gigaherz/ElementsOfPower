package gigaherz.elementsofpower.database.graph;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.item.Item;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemGraph<T>
{
    private final Map<Item, Neuron<T>> nodes = Maps.newHashMap();

    public Stream<Map.Entry<Item, Neuron<T>>> getEntries()
    {
        return nodes.entrySet().stream();
    }

    public Stream<Item> getKeys()
    {
        return nodes.keySet().stream();
    }

    public Stream<Neuron<T>> getValues()
    {
        return nodes.values().stream();
    }

    public void addEdgeBundle(Item consumer, double scale, Iterable<Map.Entry<Item, Double>> to)
    {
        Neuron<T> consumerNode = getOrCreateNode(consumer);
        Axon<T> consumerEdge = new Axon<>(consumerNode);
        consumerNode.providers.add(consumerEdge);
        for(Map.Entry<Item, Double> cs : to)
        {
            Neuron<T> providerNode = getOrCreateNode(cs.getKey());
            double edgeWeight = cs.getValue()/scale;
            Terminal<T> target = new Terminal<>(consumerEdge, providerNode, edgeWeight);
            consumerEdge.providers.add(target);
            providerNode.consumers.add(target);
        }
    }

    private Neuron<T> getOrCreateNode(Item from)
    {
        return nodes.computeIfAbsent(from, Neuron::new);
    }

    public interface ScaleFunction<T>
    {
        T scale(T original, double scale);
    }

    public void floodFill(Item key, T fillWith, ScaleFunction<T> multiply, BiFunction<T, T, T> adder, BiPredicate<T, T> replace)
    {
        Neuron<T> start = nodes.get(key);
        if (start == null)
            return;

        Queue<Neuron<T>> dirty = new ArrayDeque<>();

        start.providedData = new Signal<>(start, fillWith);
        dirty.add(start);

        while(dirty.size() > 0)
        {
            Neuron<T> current = dirty.remove();

            boolean isDirty = false;

            if (current.providedData != null && (current.fillData == null || replace.test(current.fillData.value, current.providedData.value)))
            {
                current.fillData = current.providedData;
                dirty.add(current);
                isDirty = true;
            }

            // FIXME: This isn't enough. Paths will need to get calculated after the fact, bottom-up, once all data has been flood-filled.
            for(Axon<T> axon : current.providers)
            {
                if (axon.computedFillData == null)
                {
                    T accV = null;
                    boolean allFound = true;
                    for(Terminal<T> provider : axon.providers)
                    {
                        if (axon.fillData == null)
                        {
                            allFound = false;
                            break;
                        }

                        Signal<T> fillData1 = axon.fillData.get(provider.provider);
                        if (fillData1 == null || fillData1.containsInParents(current))
                        {
                            allFound = false;
                            break;
                        }

                        if (accV != null)
                            accV = adder.apply(accV, multiply.scale(fillData1.value, provider.edgeWeight));
                        else
                            accV = fillData1.value;
                    }

                    if(allFound && accV != null && (axon.computedFillData == null || replace.test(axon.computedFillData.value, accV)))
                    {
                        axon.computedFillData = new Signal<>(current, accV, axon.fillData.values());
                        if (!isDirty) dirty.add(current);
                        isDirty = true;
                    }
                }

                if (axon.computedFillData != null)
                {
                    Signal<T> axonFillData = axon.computedFillData;

                    if (current.fillData == null || replace.test(current.fillData.value, axonFillData.value))
                    {
                        current.fillData = new Signal<>(current, axonFillData.value, axonFillData);
                        if (!isDirty) dirty.add(current);
                        isDirty = true;
                    }
                }
            }

            if (current.fillData != null && isDirty)
            {
                for (Terminal<T> terminal : current.consumers)
                {
                    Axon<T> axon = terminal.owner;
                    Neuron<T> consumer = axon.owner;
                    Signal<T> fillData2 = consumer.fillData;

                    if (fillData2 != null && fillData2.contains(current))
                        continue;

                    T value = multiply.scale(current.fillData.value, terminal.edgeWeight);

                    Signal<T> fillDataSource = axon.fillData == null ? null : axon.fillData.get(current);
                    if (fillDataSource == null || replace.test(fillDataSource.value, value))
                    {
                        if (axon.fillData == null)
                            axon.fillData = Maps.newHashMap();
                        axon.fillData.put(current, new Signal<>(consumer, value, current.fillData));
                        axon.computedFillData = null;
                        dirty.add(consumer);
                    }
                }
            }
        }
    }

    public void finishFill(BiConsumer<Item, T> consumer)
    {
        nodes.values().forEach(value -> {
            if (value.fillData != null)
            {
                consumer.accept(value.owner, value.fillData.value);
            }
            value.providedData = null;
            value.fillData = null;
            for(Axon<T> axon : value.providers)
            {
                axon.fillData = null;
                axon.computedFillData = null;
            }
        });
    }

    public static class Neuron<T>
    {
        public final Item owner;
        public final Set<Axon<T>> providers = Sets.newHashSet();
        public final Set<Terminal<T>> consumers = Sets.newHashSet();

        public Signal<T> providedData;
        public Signal<T> fillData;

        private Neuron(Item owner)
        {
            this.owner = owner;
        }

        @Override
        public String toString()
        {
            return String.format("{%s}", owner);
        }
    }

    public static class Axon<T>
    {
        public final Neuron<T> owner;
        public final Set<Terminal<T>> providers = Sets.newHashSet();

        public Map<Neuron<T>,Signal<T>> fillData;
        public Signal<T> computedFillData = null;

        private Axon(Neuron<T> owner)
        {
            this.owner = owner;
        }

        @Override
        public String toString()
        {
            return String.format("{Edge: %s -> %s}", owner, providers.stream().map(t -> t.provider.toString()).collect(Collectors.joining(",")));
        }
    }

    public static class Terminal<T>
    {
        public final Axon<T> owner;
        public final Neuron<T> provider;
        public final double edgeWeight;

        private Terminal(Axon<T> owner, Neuron<T> provider, double edgeWeight)
        {
            this.owner = owner;
            this.provider = provider;
            this.edgeWeight = edgeWeight;
        }

        @Override
        public String toString()
        {
            return String.format("{Edge: to %s weight %s from %s}", edgeWeight, provider, owner);
        }
    }

    public static class Signal<T>
    {
        public final Neuron<T> owner;
        public final Set<Signal<T>> via = Sets.newHashSet();
        public T value;

        public Signal(Neuron<T> owner, T value)
        {
            this.owner = owner;
            this.value = value;
        }

        public Signal(Neuron<T> owner, T value, Signal<T> via)
        {
            this(owner,value);
            this.via.add(Objects.requireNonNull(via));
        }

        public Signal(Neuron<T> owner, T value, Collection<Signal<T>> signals)
        {
            this(owner,value);
            signals.forEach(Objects::requireNonNull);
            this.via.addAll(signals);
        }

        public boolean contains(Neuron<T> node)
        {
            if (owner == node)
                return true;
            for(Signal<T> v : via)
            {
                if (v.contains(node))
                    return true;
            }
            return false;
        }

        public boolean containsInParents(Neuron<T> node)
        {
            for(Signal<T> v : via)
            {
                if (v.contains(node))
                    return true;
            }
            return false;
        }

        @Override
        public String toString()
        {
            return String.format("{Fill: %s as %s via %s}", owner, value, via);
        }
    }
}
