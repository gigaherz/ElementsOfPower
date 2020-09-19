package gigaherz.elementsofpower.magic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class MagicGradient
{
    public static final Codec<MagicGradient> CODEC = RecordCodecBuilder
            .create((instance) -> instance.group(
                    GradientPoint.CODEC.listOf().fieldOf("points").forGetter(i -> ImmutableList.copyOf(i.points))
            ).apply(instance, MagicGradient::new));

    private final List<GradientPoint> points;

    private MagicGradient(List<GradientPoint> points)
    {
        this.points = points;
    }

    public MagicAmounts getAt(float point)
    {
        GradientPoint pt0 = points.get(0);

        if (point < pt0.point)
            return pt0.value;

        for(int i=1;i<points.size();i++)
        {
            GradientPoint pt1 = points.get(i);
            if (point < pt1.point)
            {
                float t = (point - pt0.point) / (pt1.point - pt0.point);

                return MagicAmounts.lerp(pt0, pt1, t);
            }
            pt0 = pt1;
        }

        return points.get(points.size()-1).value;
    }

    public static class GradientPoint
    {
        public static final Codec<GradientPoint> CODEC = RecordCodecBuilder
                .create((instance) -> instance.group(
                        Codec.FLOAT.fieldOf("point").forGetter(i -> i.point),
                        MagicAmounts.CODEC.fieldOf("value").forGetter(i -> i.value)
                ).apply(instance, GradientPoint::new));

        public final float point;
        public final MagicAmounts value;

        private GradientPoint(float point, MagicAmounts value)
        {
            this.point = point;
            this.value = value;
        }
    }

    public static class Builder
    {
        private final List<GradientPoint> list = Lists.newArrayList();
        private GradientPoint last = null;

        public Builder addPoint(float p, MagicAmounts am)
        {
            if (last != null)
            {
                if (p < last.point)
                    throw new IllegalArgumentException(String.format("Parameter p=%f must be >= the last added point (%f)", p, last.point));
            }
            list.add(last = new GradientPoint(p, am));
            return this;
        }

        public MagicGradient build()
        {
            return new MagicGradient(list);
        }
    }
}
