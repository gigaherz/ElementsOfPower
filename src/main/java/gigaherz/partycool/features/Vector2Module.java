package gigaherz.partycool.features;

import gigaherz.partycool.ParticleModule;
import gigaherz.partycool.ParticleSystem;
import gigaherz.partycool.values.VaryingNumber;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines the position of the particle
 */
public class Vector2Module extends ParticleModule
{
    private final String componentName;
    private final VaryingNumber u;
    private final VaryingNumber v;
    private Supplier<float[]> xChannel;
    private Supplier<float[]> yChannel;
    private Supplier<float[]> xInitialChannel;
    private Supplier<float[]> yInitialChannel;
    private Supplier<float[]> timeChannel;

    public Vector2Module(String componentName, VaryingNumber u, VaryingNumber v)
    {
        this.componentName = componentName;
        super.addConsumes("time", componentName + ".u.initial", componentName + ".v.initial");
        super.addModifies(componentName + ".u", componentName + ".v");
        this.u = u;
        this.v = v;
    }

    @Override
    public void setIndices(Function<String, Supplier<float[]>> indices)
    {
        timeChannel = indices.apply("time");
        xInitialChannel = indices.apply(componentName + ".u.initial");
        yInitialChannel = indices.apply(componentName + ".v.initial");
        xChannel = indices.apply(componentName + ".u");
        yChannel = indices.apply(componentName + ".v");
    }

    @Override
    public void update(ParticleSystem system, int usedSize)
    {
        float[] times = timeChannel.get();

        // initial value (in)
        float[] xs = xInitialChannel.get();
        float[] ys = yInitialChannel.get();

        // current value (out)
        float[] xc = xChannel.get();
        float[] yc = yChannel.get();

        for (int i = 0; i < usedSize; i++)
        {
            xc[i] = xs[i] + u.getValue(times[i]);
            yc[i] = ys[i] + v.getValue(times[i]);
        }
    }
}
