package dev.gigaherz.purrticles.features;

import dev.gigaherz.purrticles.ParticleModule;
import dev.gigaherz.purrticles.ParticleSystem;
import dev.gigaherz.purrticles.values.VaryingNumber;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines the position of the particle
 */
public class SingleValueModule extends ParticleModule
{
    private final String componentName;
    private final VaryingNumber value;
    private Supplier<float[]> currentChannel;
    private Supplier<float[]> initialChannel;
    private Supplier<float[]> timeChannel;

    public SingleValueModule(String componentName, VaryingNumber value)
    {
        this.componentName = componentName;
        super.addConsumes("time", componentName + ".initial");
        super.addModifies(componentName);
        this.value = value;
    }

    @Override
    public void setIndices(Function<String, Supplier<float[]>> indices)
    {
        timeChannel = indices.apply("time");
        initialChannel = indices.apply(componentName + ".initial");
        currentChannel = indices.apply(componentName);
    }

    @Override
    public void update(ParticleSystem system, int usedSize)
    {
        float[] times = timeChannel.get();

        // initial value (in)
        float[] vs = initialChannel.get();

        // current value (out)
        float[] vc = currentChannel.get();

        for (int i = 0; i < usedSize; i++)
        {
            vc[i] = vs[i] + value.getValue(times[i]);
        }
    }
}
