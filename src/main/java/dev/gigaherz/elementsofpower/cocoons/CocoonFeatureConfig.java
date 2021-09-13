package dev.gigaherz.elementsofpower.cocoons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.magic.MagicGradient;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CocoonFeatureConfig implements FeatureConfiguration
{
    public static final Codec<CocoonFeatureConfig> CODEC = RecordCodecBuilder
            .create((instance) -> instance.group(
                    MagicGradient.CODEC.fieldOf("gradient").forGetter(i -> i.gradient)
            ).apply(instance, CocoonFeatureConfig::new));

    public static final CocoonFeatureConfig OVERWORLD = new CocoonFeatureConfig(new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY.darkness(0.25f))
            .addPoint(1, MagicAmounts.EMPTY.light(0.25f))
            .addPoint(1, MagicAmounts.EMPTY.light(1))
            .build());

    public static final CocoonFeatureConfig THE_NETHER = new CocoonFeatureConfig(new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY.fire(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.fire(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.darkness(1))
            .build());

    public static final CocoonFeatureConfig THE_END = new CocoonFeatureConfig(new MagicGradient.Builder()
            .addPoint(0, MagicAmounts.EMPTY.darkness(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.darkness(0.5f))
            .addPoint(1, MagicAmounts.EMPTY.darkness(1))
            .build());

    private final MagicGradient gradient;

    public CocoonFeatureConfig(MagicGradient gradient)
    {
        this.gradient = gradient;
    }

    public MagicAmounts getAt(float y)
    {
        return gradient.getAt(y);
    }
}
