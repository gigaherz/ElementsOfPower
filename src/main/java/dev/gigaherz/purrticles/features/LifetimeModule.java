package dev.gigaherz.purrticles.features;

import dev.gigaherz.purrticles.ParticleModule;
import dev.gigaherz.purrticles.ParticleSystem;
import dev.gigaherz.purrticles.values.VaryingNumber;

import java.util.function.Function;
import java.util.function.Supplier;

public class LifetimeModule extends ParticleModule
{
    private final VaryingNumber lifetime;
    private Supplier<float[]> timeChannel;

    public LifetimeModule(VaryingNumber lifetime)
    {
        this.lifetime = lifetime;
        addConsumes("time");
    }

    @Override
    public void setIndices(Function<String, Supplier<float[]>> indices)
    {
        this.timeChannel = indices.apply("time");
    }

    @Override
    public void update(ParticleSystem system, int usedSize)
    {
        float[] times = timeChannel.get();
        for (int i = 0; i < usedSize; i++)
        {
            float life = lifetime.getValue(times[i]);
            if (life < 0)
                system.removeParticle(i);
        }
    }
}
