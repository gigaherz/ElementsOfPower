package dev.gigaherz.partycool;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ParticleModule
{
    private final List<String> consumes = Lists.newArrayList();
    private final List<String> modifies = Lists.newArrayList();

    protected ParticleModule()
    {
    }

    protected final void addConsumes(String... channelNames)
    {
        for (String str : channelNames)
        {
            if (consumes.contains(str))
                throw new IllegalStateException("Channel " + str + " is already in the consumes list.");
            consumes.addAll(Arrays.asList(channelNames));
        }
    }

    protected final void addModifies(String... channelNames)
    {
        for (String str : channelNames)
        {
            if (modifies.contains(str))
                throw new IllegalStateException("Channel " + str + " is already in the modifies list.");
            modifies.addAll(Arrays.asList(channelNames));
        }
    }

    public final Pair<List<String>, List<String>> getChannels()
    {
        return Pair.of(consumes, modifies);
    }

    public abstract void setIndices(Function<String, Supplier<float[]>> indices);

    public abstract void update(ParticleSystem system, int usedSize);
}
