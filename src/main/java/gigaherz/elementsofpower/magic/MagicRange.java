package gigaherz.elementsofpower.magic;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MagicRange
{
    public static final Codec<MagicRange> CODEC = RecordCodecBuilder
            .create((instance) -> instance.group(
                    MagicAmounts.CODEC.fieldOf("min").forGetter(i -> i.min),
                    MagicAmounts.CODEC.fieldOf("max").forGetter(ceil -> ceil.max)
            ).apply(instance, MagicRange::new));

    public static MagicRange between(MagicAmounts min, MagicAmounts max)
    {
        return new MagicRange(min, max);
    }

    private final MagicAmounts min;
    private final MagicAmounts max;

    private MagicRange(MagicAmounts min, MagicAmounts max)
    {
        if (!min.lessEqual(max))
            throw new IllegalArgumentException("The amounts in min must be smaller or equal than the amounts in max.");
        this.min = min;
        this.max = max;
    }

    public MagicAmounts getMin()
    {
        return min;
    }

    public MagicAmounts getMax()
    {
        return max;
    }

}
