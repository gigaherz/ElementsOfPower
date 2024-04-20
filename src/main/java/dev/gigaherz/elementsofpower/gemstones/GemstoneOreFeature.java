package dev.gigaherz.elementsofpower.gemstones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.neoforged.neoforge.common.Tags;

import java.util.Map;

public class GemstoneOreFeature extends Feature<GemstoneOreFeature.Configuration>
{
    private final OreFeature innerFeature = new OreFeature(OreConfiguration.CODEC);

    public GemstoneOreFeature(Codec<Configuration> pCodec)
    {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GemstoneOreFeature.Configuration> pContext)
    {
        var level = pContext.level();
        var pos = pContext.origin();
        var biome = level.getBiome(pos);

        // get values from biome
        var isHot = biome.is(Tags.Biomes.IS_HOT_OVERWORLD);
        var isCold = biome.is(Tags.Biomes.IS_COLD_OVERWORLD);
        var isWet = biome.is(Tags.Biomes.IS_WET_OVERWORLD);
        var isDry = biome.is(Tags.Biomes.IS_DRY_OVERWORLD);
        var isDense = biome.is(Tags.Biomes.IS_DENSE_OVERWORLD);
        var isSparse = biome.is(Tags.Biomes.IS_SPARSE_OVERWORLD);

        var heat = BiomeValue.from(isHot, isCold);
        var humidity = BiomeValue.from(isWet, isDry);
        var life = BiomeValue.from(isDense, isSparse);
        var values = new BiomeValues(heat, humidity, life);

        var config = pContext.config().configurations.get(values);

        return innerFeature.place(new FeaturePlaceContext<>(pContext.topFeature(), pContext.level(), pContext.chunkGenerator(), pContext.random(), pContext.origin(),  config));
    }

    public record Configuration(Map<BiomeValues, OreConfiguration> configurations) implements FeatureConfiguration
    {
        public static final Codec<GemstoneOreFeature.Configuration> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(Codec.unboundedMap(BiomeValues.STRING_CODEC, OreConfiguration.CODEC)
                        .fieldOf("configurations").forGetter(obj -> obj.configurations)
                ).apply(inst, Configuration::new));
    }
}
