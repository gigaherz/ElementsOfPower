package dev.gigaherz.partycool.features;

import dev.gigaherz.partycool.ParticleModule;
import dev.gigaherz.partycool.ParticleSystem;
import dev.gigaherz.partycool.values.VaryingNumber;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines the position of the particle
 */
public class ColorModule extends ParticleModule
{
    private final String componentName;
    private final VaryingNumber r;
    private final VaryingNumber g;
    private final VaryingNumber b;
    private final VaryingNumber a;
    private Supplier<float[]> rChannel;
    private Supplier<float[]> gChannel;
    private Supplier<float[]> bChannel;
    private Supplier<float[]> aChannel;
    private Supplier<float[]> rInitialChannel;
    private Supplier<float[]> gInitialChannel;
    private Supplier<float[]> bInitialChannel;
    private Supplier<float[]> aInitialChannel;
    private Supplier<float[]> timeChannel;

    public ColorModule(String componentName, VaryingNumber r, VaryingNumber g, VaryingNumber b, VaryingNumber a)
    {
        this.componentName = componentName;
        super.addConsumes("time",
                componentName + ".r.initial",
                componentName + ".g.initial",
                componentName + ".b.initial",
                componentName + ".a.initial");
        super.addModifies(
                componentName + ".r",
                componentName + ".g",
                componentName + ".b",
                componentName + ".a");
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void setIndices(Function<String, Supplier<float[]>> indices)
    {
        timeChannel = indices.apply("time");
        rInitialChannel = indices.apply(componentName + ".r.initial");
        gInitialChannel = indices.apply(componentName + ".g.initial");
        bInitialChannel = indices.apply(componentName + ".b.initial");
        aInitialChannel = indices.apply(componentName + ".a.initial");
        rChannel = indices.apply(componentName + ".r");
        gChannel = indices.apply(componentName + ".g");
        bChannel = indices.apply(componentName + ".b");
        aChannel = indices.apply(componentName + ".a");
    }

    @Override
    public void update(ParticleSystem system, int usedSize)
    {
        float[] times = timeChannel.get();

        // initial value (in)
        float[] rs = rInitialChannel.get();
        float[] gs = gInitialChannel.get();
        float[] bs = bInitialChannel.get();
        float[] as = aInitialChannel.get();

        // current value (out)
        float[] rc = rChannel.get();
        float[] gc = gChannel.get();
        float[] bc = bChannel.get();
        float[] ac = aChannel.get();

        for (int i = 0; i < usedSize; i++)
        {
            rc[i] = rs[i] + r.getValue(times[i]);
            gc[i] = gs[i] + g.getValue(times[i]);
            bc[i] = bs[i] + b.getValue(times[i]);
            ac[i] = as[i] + a.getValue(times[i]);
        }
    }
}
