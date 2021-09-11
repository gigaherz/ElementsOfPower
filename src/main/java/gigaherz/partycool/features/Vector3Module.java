package gigaherz.partycool.features;

import gigaherz.partycool.ParticleModule;
import gigaherz.partycool.ParticleSystem;
import gigaherz.partycool.values.VaryingNumber;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines the position of the particle
 */
public class Vector3Module extends ParticleModule
{
    private final String componentName;
    private final VaryingNumber x;
    private final VaryingNumber y;
    private final VaryingNumber z;
    private Supplier<float[]> xChannel;
    private Supplier<float[]> yChannel;
    private Supplier<float[]> zChannel;
    private Supplier<float[]> xInitialChannel;
    private Supplier<float[]> yInitialChannel;
    private Supplier<float[]> zInitialChannel;
    private Supplier<float[]> timeChannel;

    public Vector3Module(String componentName, VaryingNumber x, VaryingNumber y, VaryingNumber z)
    {
        this.componentName = componentName;
        super.addConsumes("time",
                componentName + ".x.initial",
                componentName + ".y.initial",
                componentName + ".z.initial");
        super.addModifies(
                componentName + ".x",
                componentName + ".y",
                componentName + ".z");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void setIndices(Function<String, Supplier<float[]>> indices)
    {
        timeChannel = indices.apply("time");
        xInitialChannel = indices.apply(componentName + ".x.initial");
        yInitialChannel = indices.apply(componentName + ".y.initial");
        zInitialChannel = indices.apply(componentName + ".z.initial");
        xChannel = indices.apply(componentName + ".x");
        yChannel = indices.apply(componentName + ".y");
        zChannel = indices.apply(componentName + ".z");
    }

    @Override
    public void update(ParticleSystem system, int usedSize)
    {
        float[] times = timeChannel.get();

        // initial value (in)
        float[] xs = xInitialChannel.get();
        float[] ys = yInitialChannel.get();
        float[] zs = zInitialChannel.get();

        // current value (out)
        float[] xc = xChannel.get();
        float[] yc = yChannel.get();
        float[] zc = zChannel.get();

        for (int i = 0; i < usedSize; i++)
        {
            xc[i] = xs[i] + x.getValue(times[i]);
            yc[i] = ys[i] + y.getValue(times[i]);
            zc[i] = zs[i] + z.getValue(times[i]);
        }
    }
}
