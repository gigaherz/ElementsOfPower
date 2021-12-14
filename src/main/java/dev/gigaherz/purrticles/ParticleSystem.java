package dev.gigaherz.purrticles;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ParticleSystem
{
    private static final int MAX_DOUBLING_SIZE = 1 << 20;

    private final Object2IntMap<String> indices;
    private final ParticleModule[] modules;
    private final float[][] arrays;
    private boolean[] removed;
    private int allocatedSize;
    private int usedSize;

    public ParticleSystem(ParticleModule[] modules, int initialSystemSize)
    {
        this.modules = modules;

        List<String> channels = Lists.newArrayList();

        channels.add("time");

        Map<String, ParticleModule> modifiers = Maps.newHashMap();
        for (ParticleModule module : modules)
        {
            Map<String, Integer> indices = Maps.newHashMap();
            Pair<List<String>, List<String>> ch = module.getChannels();
            for (String str : ch.getFirst())
            {
                if (!channels.contains(str)) channels.add(str);
                indices.computeIfAbsent(str, s -> channels.indexOf(str));
            }

            for (String str : ch.getFirst())
            {
                if (!channels.contains(str)) channels.add(str);
                if (modifiers.get(str) != null)
                    throw new IllegalStateException("Modules " + modifiers.get(str) + " and " + module + " both want to modify channel " + str);
                modifiers.put(str, module);
                indices.computeIfAbsent(str, s -> channels.indexOf(str));
            }

            module.setIndices(name -> {
                int index = indices.getOrDefault(name, -1);
                if (index >= 0) return () -> this.getArrays()[index];
                return () -> null;
            });
        }

        this.arrays = new float[channels.size()][];

        for (int i = 0; i < arrays.length; i++)
        {
            this.arrays[i] = new float[initialSystemSize];
        }

        this.removed = new boolean[initialSystemSize];
        this.allocatedSize = initialSystemSize;
        this.usedSize = 0;

        this.indices = new Object2IntArrayMap<>();
        for (int i = 0; i < channels.size(); i++)
        {
            indices.put(channels.get(i), i);
        }
    }

    public void addParticle(float... datas)
    {
        if (datas.length != arrays.length)
        {
            throw new IllegalStateException(String.format("The number of parameters (%d) does not match the number of channels in the system (%d)", datas.length, arrays.length));
        }

        if (usedSize == allocatedSize)
        {
            resizeArrays();
        }

        for (int i = 0; i < arrays.length; i++)
        {
            this.arrays[i][usedSize] = datas[i];
        }

        removed[usedSize] = false;

        usedSize++;
    }

    private void resizeArrays()
    {
        allocatedSize += Math.min(allocatedSize, MAX_DOUBLING_SIZE);
        for (int i = 0; i < arrays.length; i++)
        {
            this.arrays[i] = Arrays.copyOf(this.arrays[i], allocatedSize);
        }
        this.removed = Arrays.copyOf(this.removed, allocatedSize);
    }

    public void removeParticle(int index)
    {
        removed[index] = true;
    }

    public void update(float timeDelta)
    {
        for (int i = 0; i < usedSize; i++)
        {
            if (!removed[i]) arrays[0][i] += timeDelta;
        }

        for (ParticleModule module : modules)
        {
            module.update(this, usedSize);
        }

        shrinkUsed();

        for (int i = 0; i < usedSize; i++)
        {
            if (removed[i])
            {
                usedSize--;

                if (removed[usedSize - 1]) throw new IllegalStateException("Last element was removed?!");

                removed[i] = true;
                for (int j = 0; j < arrays.length; j++)
                {
                    this.arrays[j][i] = this.arrays[j][usedSize - 1];
                }

                shrinkUsed();
            }
        }
    }

    private void shrinkUsed()
    {
        while (usedSize > 0 && removed[usedSize - 1])
        {
            usedSize--;
        }
    }

    public float[][] getArrays()
    {
        return arrays;
    }

    public Object2IntMap<String> getIndices()
    {
        return indices;
    }

    public <T extends ParticleModule> T getModule(Class<T> moduleClass)
    {
        for (ParticleModule module : modules)
        {
            if (moduleClass.isInstance(module))
            {
                //noinspection unchecked
                return (T) module;
            }
        }
        return null;
    }

    public int size()
    {
        return usedSize;
    }
}
